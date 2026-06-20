param(
    [string]$BackendBaseUrl = "http://localhost:8080/api/v1",
    [string]$SeedPath = "",
    [string]$OutputPath = "",
    [string]$CacheDir = "",
    [int]$MaxSizeMb = 900,
    [switch]$UseArchiveSearch,
    [string[]]$ArchiveCollections = @("feature_films", "animationandcartoons", "prelinger"),
    [int]$RowsPerCollection = 10,
    [int]$MaxAutoItems = 20,
    [switch]$VerifyOnly,
    [switch]$KeepDownloads
)

$ErrorActionPreference = "Stop"

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($SeedPath)) {
    $SeedPath = Join-Path $ProjectRoot "data\media-crawl-seeds.json"
}
if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $ProjectRoot "data\media-upload-results.json"
}
if ([string]::IsNullOrWhiteSpace($CacheDir)) {
    $CacheDir = Join-Path $ProjectRoot ".media-cache"
}

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $OutputPath) | Out-Null
New-Item -ItemType Directory -Force -Path $CacheDir | Out-Null

function ConvertTo-SafeFileName {
    param([string]$Name)
    $safe = $Name -replace '[\\/:*?"<>|]', '-'
    $safe = $safe -replace '\s+', '_'
    return $safe.Trim('_')
}

function Get-ArchiveMp4Candidate {
    param($Seed)

    $metadataUrl = "https://archive.org/metadata/$($Seed.identifier)"
    Write-Host "Reading metadata: $metadataUrl"
    $metadata = Invoke-RestMethod -Uri $metadataUrl -TimeoutSec 30
    $mp4Files = @($metadata.files | Where-Object { $_.name -match '\.mp4$' })
    if ($mp4Files.Count -eq 0) {
        throw "No .mp4 files found for archive identifier '$($Seed.identifier)'."
    }

    $selected = $null
    if ($Seed.preferredFile) {
        $selected = $mp4Files | Where-Object { $_.name -eq $Seed.preferredFile } | Select-Object -First 1
        if (-not $selected) {
            throw "Preferred file '$($Seed.preferredFile)' was not found for '$($Seed.identifier)'."
        }
    } else {
        $maxBytes = [int64]$MaxSizeMb * 1MB
        $selected = $mp4Files |
            Where-Object { $_.size -and ([int64]$_.size) -le $maxBytes } |
            Sort-Object { [int64]$_.size } -Descending |
            Select-Object -First 1
        if (-not $selected) {
            $selected = $mp4Files | Sort-Object {
                if ($_.size) { [int64]$_.size } else { [int64]0 }
            } -Descending | Select-Object -First 1
        }
    }

    $encodedName = [uri]::EscapeDataString($selected.name).Replace("%2F", "/")
    [pscustomobject]@{
        Url = "https://archive.org/download/$($Seed.identifier)/$encodedName"
        FileName = $selected.name
        SizeBytes = if ($selected.size) { [int64]$selected.size } else { $null }
    }
}

function Get-ArchiveCategory {
    param([string]$Collection)

    switch ($Collection) {
        "animationandcartoons" { return "Animation" }
        "prelinger" { return "Documentary" }
        "sports" { return "Sports" }
        default { return "Movie" }
    }
}

function Get-ArchiveSearchSeeds {
    param(
        [string[]]$Collections,
        [int]$Rows,
        [int]$Limit
    )

    $autoSeeds = New-Object System.Collections.Generic.List[object]
    $seen = @{}

    foreach ($collection in $Collections) {
        if ($Limit -gt 0 -and $autoSeeds.Count -ge $Limit) {
            break
        }

        $query = "collection:($collection) AND mediatype:(movies)"
        $encodedQuery = [uri]::EscapeDataString($query)
        $searchUrl = "https://archive.org/advancedsearch.php?q=$encodedQuery&fl[]=identifier&fl[]=title&fl[]=subject&fl[]=year&rows=$Rows&page=1&sort[]=downloads+desc&output=json"
        Write-Host "Searching archive collection: $collection"
        $search = Invoke-RestMethod -Uri $searchUrl -TimeoutSec 45

        foreach ($doc in @($search.response.docs)) {
            if ($Limit -gt 0 -and $autoSeeds.Count -ge $Limit) {
                break
            }
            if (-not $doc.identifier -or $seen.ContainsKey($doc.identifier)) {
                continue
            }

            $seen[$doc.identifier] = $true
            $autoSeeds.Add([pscustomobject]@{
                title = if ($doc.title) { [string]$doc.title } else { [string]$doc.identifier }
                category = Get-ArchiveCategory -Collection $collection
                type = "SINGLE"
                source = "archive"
                identifier = [string]$doc.identifier
                preferredFile = $null
                folder = "films"
                year = $doc.year
                subjects = $doc.subject
                collection = $collection
            })
        }
    }

    return $autoSeeds.ToArray()
}

function Test-DirectMp4Url {
    param([string]$Url)

    $headers = & curl.exe -L -I --silent --show-error --max-time 30 $Url 2>&1
    $text = ($headers -join "`n")
    $statusMatches = [regex]::Matches($text, 'HTTP/\S+\s+(\d{3})')
    $typeMatches = [regex]::Matches($text, '(?im)^content-type:\s*([^\r\n;]+)')
    $lengthMatches = [regex]::Matches($text, '(?im)^content-length:\s*(\d+)')
    $rangeMatches = [regex]::Matches($text, '(?im)^accept-ranges:\s*bytes')

    $status = if ($statusMatches.Count -gt 0) { [int]$statusMatches[$statusMatches.Count - 1].Groups[1].Value } else { 0 }
    $contentType = if ($typeMatches.Count -gt 0) { $typeMatches[$typeMatches.Count - 1].Groups[1].Value.Trim() } else { "" }
    $contentLength = if ($lengthMatches.Count -gt 0) { [int64]$lengthMatches[$lengthMatches.Count - 1].Groups[1].Value } else { $null }
    $acceptRanges = $rangeMatches.Count -gt 0
    $isValid = $status -eq 200 -and $contentType -eq "video/mp4"

    [pscustomobject]@{
        IsValid = $isValid
        Status = $status
        ContentType = $contentType
        ContentLength = $contentLength
        AcceptRanges = $acceptRanges
        RawHeaders = $text
    }
}

function Download-MediaFile {
    param(
        [string]$Url,
        [string]$DestinationPath
    )
    Write-Host "Downloading to $DestinationPath"
    & curl.exe -L --fail --progress-bar --output $DestinationPath $Url
    if ($LASTEXITCODE -ne 0) {
        throw "Download failed for $Url"
    }
}

function Upload-MediaFile {
    param(
        [string]$FilePath,
        [string]$Folder
    )

    $uploadUrl = ($BackendBaseUrl.TrimEnd('/')) + "/files/upload"
    Write-Host "Uploading to $uploadUrl (folder=$Folder)"
    $response = & curl.exe --silent --show-error --fail -X POST `
        -F "file=@$FilePath" `
        -F "folder=$Folder" `
        $uploadUrl
    if ($LASTEXITCODE -ne 0) {
        throw "Upload failed for $FilePath"
    }
    $json = $response | ConvertFrom-Json
    return $json.data
}

if ($UseArchiveSearch) {
    $seeds = Get-ArchiveSearchSeeds -Collections $ArchiveCollections -Rows $RowsPerCollection -Limit $MaxAutoItems
} else {
    $seeds = Get-Content -LiteralPath $SeedPath -Raw -Encoding UTF8 | ConvertFrom-Json
}
$results = New-Object System.Collections.Generic.List[object]
$rejected = New-Object System.Collections.Generic.List[object]

foreach ($seed in $seeds) {
    Write-Host ""
    Write-Host "==> $($seed.title) [$($seed.category)]"

    try {
        if ($seed.source -eq "archive") {
            $candidate = Get-ArchiveMp4Candidate -Seed $seed
            $sourceUrl = $candidate.Url
            $fileName = $candidate.FileName
        } elseif ($seed.source -eq "direct") {
            $sourceUrl = $seed.sourceUrl
            $fileName = Split-Path ([uri]$sourceUrl).AbsolutePath -Leaf
        } else {
            throw "Unknown source '$($seed.source)'"
        }

        $verified = Test-DirectMp4Url -Url $sourceUrl
        if (-not $verified.IsValid) {
            $rejected.Add([pscustomobject]@{
                title = $seed.title
                category = $seed.category
                sourceUrl = $sourceUrl
                status = $verified.Status
                contentType = $verified.ContentType
                reason = "Not a direct video/mp4 URL"
            })
            Write-Warning "Rejected: status=$($verified.Status), contentType=$($verified.ContentType)"
            continue
        }

        $sizeMb = if ($verified.ContentLength) { [math]::Round($verified.ContentLength / 1MB, 2) } else { $null }
        if ($verified.ContentLength -and $verified.ContentLength -gt ([int64]$MaxSizeMb * 1MB)) {
            $rejected.Add([pscustomobject]@{
                title = $seed.title
                category = $seed.category
                sourceUrl = $sourceUrl
                status = $verified.Status
                contentType = $verified.ContentType
                sizeMb = $sizeMb
                reason = "File exceeds MaxSizeMb=$MaxSizeMb"
            })
            Write-Warning "Rejected: size ${sizeMb}MB exceeds MaxSizeMb=$MaxSizeMb"
            continue
        }

        $uploadedUrl = $null
        $localPath = $null
        if (-not $VerifyOnly) {
            $safeBaseName = ConvertTo-SafeFileName -Name $seed.title
            $extension = [System.IO.Path]::GetExtension($fileName)
            if ([string]::IsNullOrWhiteSpace($extension)) {
                $extension = ".mp4"
            }
            $localPath = Join-Path $CacheDir "$safeBaseName$extension"
            if (-not (Test-Path -LiteralPath $localPath)) {
                Download-MediaFile -Url $sourceUrl -DestinationPath $localPath
            } else {
                Write-Host "Using cached file: $localPath"
            }
            $uploadedUrl = Upload-MediaFile -FilePath $localPath -Folder $seed.folder
            if (-not $KeepDownloads) {
                Remove-Item -LiteralPath $localPath -Force
            }
        }

        $results.Add([pscustomobject]@{
            title = $seed.title
            category = $seed.category
            type = $seed.type
            source = $seed.source
            identifier = $seed.identifier
            collection = $seed.collection
            year = $seed.year
            sourceUrl = $sourceUrl
            minioUrl = $uploadedUrl
            folder = $seed.folder
            sizeBytes = $verified.ContentLength
            sizeMb = $sizeMb
            contentType = $verified.ContentType
            acceptRanges = $verified.AcceptRanges
        })
    } catch {
        $rejected.Add([pscustomobject]@{
            title = $seed.title
            category = $seed.category
            reason = $_.Exception.Message
        })
        Write-Warning $_.Exception.Message
    }
}

$output = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    verifyOnly = [bool]$VerifyOnly
    backendBaseUrl = $BackendBaseUrl
    accepted = $results
    rejected = $rejected
}

$output | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $OutputPath -Encoding UTF8
Write-Host ""
Write-Host "Done. Accepted=$($results.Count), Rejected=$($rejected.Count)"
Write-Host "Output: $OutputPath"
