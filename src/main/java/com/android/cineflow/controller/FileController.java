package com.android.cineflow.controller;

import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.service.storage.IStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.prefix}/files")
@RequiredArgsConstructor
public class FileController {

    private final IStorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "") String folder) {
        String fileUrl = storageService.uploadFile(file, folder);
        return ResponseEntity.ok(ApiResponse.success("Tải tệp tin lên thành công", fileUrl));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam("url") String fileUrl) {
        storageService.deleteFile(fileUrl);
        return ResponseEntity.ok(ApiResponse.success("Xóa tệp tin thành công", null));
    }
}
