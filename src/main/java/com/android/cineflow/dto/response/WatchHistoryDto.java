package com.android.cineflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WatchHistoryDto {
    private Integer id;
    private FilmResponseDto film;
    private EpisodeDto episode;
    private Integer resumePositionSeconds;
    private LocalDateTime lastWatchedAt;
}
