package com.android.cineflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortsDto {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private Integer duration;
    private Integer viewCount;
}

