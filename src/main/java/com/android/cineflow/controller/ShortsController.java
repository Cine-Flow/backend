package com.android.cineflow.controller;

import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.ShortsResponse;
import com.android.cineflow.service.shorts.IShortsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class ShortsController {

    private final IShortsService shortsService;

    @GetMapping("/films/shorts")
    public ResponseEntity<ApiResponse<ShortsResponse>> getShorts() {
        return ResponseEntity.ok(ApiResponse.success("Shorts fetched", shortsService.getShorts()));
    }
}

