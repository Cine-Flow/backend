package com.android.cineflow.controller;

import com.android.cineflow.dto.request.*;
import com.android.cineflow.dto.response.*;
import com.android.cineflow.service.user.UserContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class UserContentController {
    private final UserContentService userContentService;

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<FavoriteDto>>> getFavorites() {
        return ResponseEntity.ok(ApiResponse.success("Favorites fetched", userContentService.getFavorites()));
    }

    @PostMapping("/favorites/{filmId}")
    public ResponseEntity<ApiResponse<FavoriteDto>> addFavorite(@PathVariable Integer filmId) {
        return ResponseEntity.ok(ApiResponse.success("Favorite added", userContentService.addFavorite(filmId)));
    }

    @DeleteMapping("/favorites/{filmId}")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(@PathVariable Integer filmId) {
        userContentService.deleteFavorite(filmId);
        return ResponseEntity.ok(ApiResponse.success("Favorite deleted"));
    }

    @GetMapping("/watch-history")
    public ResponseEntity<ApiResponse<List<WatchHistoryDto>>> getWatchHistory() {
        return ResponseEntity.ok(ApiResponse.success("Watch history fetched", userContentService.getWatchHistory()));
    }

    @PutMapping("/watch-history/{episodeId}")
    public ResponseEntity<ApiResponse<WatchHistoryDto>> updateWatchHistory(
            @PathVariable Integer episodeId, @Valid @RequestBody UpdateWatchHistoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Watch history updated",
                userContentService.updateWatchHistory(episodeId, request)));
    }

    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", userContentService.getProfile()));
    }

    @PutMapping("/user/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Hồ sơ đã được cập nhật thành công", userContentService.updateProfile(request)));
    }

    @PostMapping("/user/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userContentService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    @GetMapping("/user/analytics")
    public ResponseEntity<ApiResponse<UserAnalyticsDto>> getUserAnalytics() {
        return ResponseEntity.ok(ApiResponse.success("User analytics fetched", userContentService.getUserAnalytics()));
    }

    @GetMapping("/subscriptions/current")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getCurrentSubscription() {
        return ResponseEntity.ok(ApiResponse.success("Subscription fetched",
                userContentService.getCurrentSubscription()));
    }
}
