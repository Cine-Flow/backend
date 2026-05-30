package com.android.cineflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FavoriteDto {
    private Integer id;
    private FilmResponseDto film;
    private LocalDateTime addedAt;
}
