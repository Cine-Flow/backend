package com.android.cineflow.controller;

import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.CategoryDto;
import com.android.cineflow.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        return ResponseEntity.ok(
                ApiResponse.success("Categories fetched", categoryService.getAllCategories()));
    }
}
