package com.android.cineflow.controller;

import com.android.cineflow.dto.request.CreateCommentRequest;
import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.CommentDto;
import com.android.cineflow.dto.response.FavoriteDto;
import com.android.cineflow.dto.response.ShortsResponse;
import com.android.cineflow.service.film.FilmCommentService;
import com.android.cineflow.service.shorts.IShortsService;
import com.android.cineflow.service.user.UserContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class ShortsController {

    private final IShortsService shortsService;
    private final UserContentService userContentService;
    private final FilmCommentService commentService;

    @GetMapping("/films/shorts")
    public ResponseEntity<ApiResponse<ShortsResponse>> getShorts() {
        return ResponseEntity.ok(ApiResponse.success("Shorts fetched", shortsService.getShorts()));
    }

    @PostMapping("/films/shorts/{filmId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FavoriteDto>> likeShort(@PathVariable Integer filmId) {
        return ResponseEntity.ok(ApiResponse.success("Short liked", userContentService.addFavorite(filmId)));
    }

    @DeleteMapping("/films/shorts/{filmId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> unlikeShort(@PathVariable Integer filmId) {
        userContentService.deleteFavorite(filmId);
        return ResponseEntity.ok(ApiResponse.success("Short unliked"));
    }

    @GetMapping("/films/shorts/{filmId}/comments")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getShortComments(@PathVariable Integer filmId) {
        return ResponseEntity.ok(ApiResponse.success("Short comments fetched", commentService.getComments(filmId)));
    }

    @PostMapping("/films/shorts/{filmId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentDto>> postShortComment(
            @PathVariable Integer filmId,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Comment posted", commentService.createComment(filmId, request)));
    }

    @DeleteMapping("/films/shorts/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteShortComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted"));
    }
}


