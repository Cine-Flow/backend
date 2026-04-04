package com.android.cineflow.service.film;

import com.android.cineflow.dto.request.CreateFilmRequest;
import com.android.cineflow.dto.request.UpdateFilmRequest;
import com.android.cineflow.dto.response.FilmDetailDto;
import com.android.cineflow.dto.response.FilmResponseDto;
import com.android.cineflow.dto.response.HomeFilmsResponse;

import java.util.List;

public interface IFilmService {
    HomeFilmsResponse getHomeFilms();
    FilmResponseDto createFilm(CreateFilmRequest request);
    List<FilmDetailDto> getAllFilms();
    FilmDetailDto getFilmById(Integer id);
    FilmDetailDto updateFilm(Integer id, UpdateFilmRequest request);
    void deleteFilm(Integer id);
}
