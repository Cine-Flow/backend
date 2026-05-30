package com.android.cineflow.dto.response.football;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballContentDto {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private String videoUrl;
    private String contentType;
    private String badge;
    private LocalDateTime publishedAt;
}
