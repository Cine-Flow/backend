package com.android.cineflow.controller;

import com.android.cineflow.dto.request.CreateCommentRequest;
import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.CommentDto;
import com.android.cineflow.service.film.FilmCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class FilmCommentController {

    private final FilmCommentService commentService;

    @GetMapping("/films/{filmId}/comments")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getComments(@PathVariable Integer filmId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Comments fetched", commentService.getComments(filmId)));
    }

    @PostMapping("/films/{filmId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable Integer filmId,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Bình luận đã được đăng", commentService.createComment(filmId, request)));
    }

    @DeleteMapping("/films/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa bình luận"));
    }
}
