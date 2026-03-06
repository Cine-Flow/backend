package com.android.cineflow.service.film;

import com.android.cineflow.dto.request.CreateFilmRequest;
import com.android.cineflow.dto.response.FilmResponseDto;
import com.android.cineflow.dto.response.HomeFilmsResponse;

public interface IFilmService {
    HomeFilmsResponse getHomeFilms();
    FilmResponseDto createFilm(CreateFilmRequest request);
}
