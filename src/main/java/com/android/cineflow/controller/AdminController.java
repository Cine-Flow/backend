package com.android.cineflow.controller;

import com.android.cineflow.dto.request.AdminCategoryRequest;
import com.android.cineflow.dto.request.AdminUserRequest;
import com.android.cineflow.dto.response.AdminAnalyticsDto;
import com.android.cineflow.dto.response.AdminCategoryDto;
import com.android.cineflow.dto.response.AdminUserDto;
import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.PagedResponse;
import com.android.cineflow.service.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<PagedResponse<AdminCategoryDto>>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success("Admin categories fetched",
                adminService.getCategoriesPaged(page, size, search)));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<AdminCategoryDto>> createCategory(@Valid @RequestBody AdminCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", adminService.createCategory(request)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<AdminCategoryDto>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody AdminCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", adminService.updateCategory(id, request)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        adminService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserDto>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success("Admin users fetched",
                adminService.getUsersPaged(page, size, search)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated", adminService.updateUser(id, request)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable String id) {
        adminService.resetUserPassword(id);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent"));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<AdminAnalyticsDto>> getAnalytics(
            @RequestParam(defaultValue = "30") int period) {
        return ResponseEntity.ok(ApiResponse.success("Admin analytics fetched", adminService.getAnalytics(period)));
    }
}
