package com.android.cineflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Integer id;
    private Integer filmId;
    private String userId;
    private String username;
    private String avatarUrl;
    private String content;
    private LocalDateTime createdAt;
}
