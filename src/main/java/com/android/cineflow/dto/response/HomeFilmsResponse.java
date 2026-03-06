package com.android.cineflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeFilmsResponse {
    private List<FilmResponseDto> banners;
    private List<FilmResponseDto> newReleases;
    private List<FilmResponseDto> sportEvents;
    private List<FilmResponseDto> hotSeries;
    private List<FilmResponseDto> dailyMovies;
}
