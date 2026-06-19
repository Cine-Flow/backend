param(
    [string]$InputPath = "",
    [string]$PsqlPath = "C:\Program Files\PostgreSQL\17\bin\psql.exe",
    [string]$DbHost = "localhost",
    [string]$DbName = "cineflow",
    [string]$DbUser = "postgres",
    [string]$DbPassword = "123456",
    [string]$TitlePrefix = "Demo - "
)

$ErrorActionPreference = "Stop"

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($InputPath)) {
    $InputPath = Join-Path $ProjectRoot "data\media-upload-results-full.json"
}

function Escape-SqlText {
    param([AllowNull()][string]$Value)
    if ($null -eq $Value) {
        return ""
    }
    return $Value.Replace("'", "''")
}

function Get-DemoThumbnailUrl {
    param($Item)
    if ($Item.identifier) {
        return "https://archive.org/services/img/$($Item.identifier)"
    }
    $seed = [uri]::EscapeDataString($Item.title)
    return "https://picsum.photos/seed/$seed/500/750"
}

if (-not (Test-Path -LiteralPath $PsqlPath)) {
    throw "psql.exe not found at '$PsqlPath'. Pass -PsqlPath if PostgreSQL is installed elsewhere."
}
if (-not (Test-Path -LiteralPath $InputPath)) {
    throw "Input file not found: $InputPath"
}

$json = Get-Content -LiteralPath $InputPath -Raw -Encoding UTF8 | ConvertFrom-Json
$items = @($json.accepted | Where-Object {
    $_.folder -eq "films" -and $_.minioUrl -and $_.type -ne "SHORT"
})
$trailers = @($json.accepted | Where-Object {
    $_.folder -eq "trailers" -and $_.minioUrl
})
$defaultTrailerUrl = if ($trailers.Count -gt 0) { [string]$trailers[0].minioUrl } else { "" }

if ($items.Count -eq 0) {
    throw "No film media rows found in $InputPath"
}

$env:PGPASSWORD = $DbPassword
$created = 0
$updated = 0

foreach ($item in $items) {
    $title = "$TitlePrefix$($item.title)"
    $description = "Demo media crawled from $($item.sourceUrl). Size: $($item.sizeMb) MB."
    $thumbnailUrl = Get-DemoThumbnailUrl -Item $item
    $trailerUrl = $defaultTrailerUrl
    $releaseYear = 2026
    $type = if ($item.type -eq "SPORTS") { "SPORTS" } else { "SINGLE" }
    $badge = if ($item.type -eq "SPORTS") { "HIGHLIGHT" } else { "DEMO_MEDIA" }

    $titleSql = Escape-SqlText $title
    $descriptionSql = Escape-SqlText $description
    $thumbnailSql = Escape-SqlText $thumbnailUrl
    $trailerSql = Escape-SqlText $trailerUrl
    $typeSql = Escape-SqlText $type
    $badgeSql = Escape-SqlText $badge
    $episodeTitleSql = Escape-SqlText "$($item.title) (Full)"
    $videoSql = Escape-SqlText $item.minioUrl

$sql = @"
WITH existing_film AS (
    SELECT id, false AS inserted FROM films WHERE title = '$titleSql' LIMIT 1
),
inserted_film AS (
    INSERT INTO films (title, description, thumbnail_url, trailer_url, release_year, is_premium, type, badge)
    SELECT '$titleSql', '$descriptionSql', '$thumbnailSql', '$trailerSql', $releaseYear, false, '$typeSql', '$badgeSql'
    WHERE NOT EXISTS (SELECT 1 FROM existing_film)
    RETURNING id, true AS inserted
),
selected_film AS (
    SELECT id, inserted FROM inserted_film
    UNION ALL
    SELECT id, inserted FROM existing_film
    LIMIT 1
),
updated_film AS (
    UPDATE films
       SET description = '$descriptionSql',
           thumbnail_url = '$thumbnailSql',
           trailer_url = '$trailerSql',
           release_year = $releaseYear,
           is_premium = false,
           type = '$typeSql',
           badge = '$badgeSql'
     WHERE id = (SELECT id FROM selected_film)
     RETURNING id
),
deleted_episode AS (
    DELETE FROM episodes
     WHERE film_id = (SELECT id FROM selected_film)
       AND episode_number = 1
),
inserted_episode AS (
    INSERT INTO episodes (film_id, episode_number, title, video_url, duration, view_count)
    VALUES ((SELECT id FROM selected_film), 1, '$episodeTitleSql', '$videoSql', 7200, 0)
    RETURNING id
),
deleted_category AS (
    DELETE FROM film_categories WHERE film_id = (SELECT id FROM selected_film)
)
SELECT id, inserted FROM selected_film;
"@

    $result = & $PsqlPath -h $DbHost -U $DbUser -d $DbName -t -A -F "|" -c $sql
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to seed '$title'"
    }

    $line = @($result | Where-Object { $_ -match '^\d+\|' } | Select-Object -First 1)
    if ($line) {
        $parts = $line -split '\|'
        $filmId = $parts[0]
        $inserted = $parts[1] -eq "t"
        if ($inserted) { $created++ } else { $updated++ }
        Write-Host "Seeded film #$filmId - $title"
    }
}

Write-Host ""
Write-Host "Done. Created=$created, Updated=$updated, Total=$($items.Count)"
