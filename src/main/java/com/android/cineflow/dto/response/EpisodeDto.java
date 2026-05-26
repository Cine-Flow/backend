package com.android.cineflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDto {
    private Integer id;
    private Integer episodeNumber;
    private String title;
    private String videoUrl;
    private Integer duration;
    private Integer viewCount;
}
