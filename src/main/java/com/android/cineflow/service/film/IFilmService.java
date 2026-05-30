package com.android.cineflow.service.film;

import com.android.cineflow.dto.request.CreateFilmRequest;
import com.android.cineflow.dto.request.UpdateFilmRequest;
import com.android.cineflow.dto.response.FilmDetailDto;
import com.android.cineflow.dto.response.FilmResponseDto;
import com.android.cineflow.dto.response.HomeFilmsResponse;
import com.android.cineflow.dto.response.PagedResponse;

import com.android.cineflow.model.enums.FilmType;

import java.util.List;

public interface IFilmService {
    HomeFilmsResponse getHomeFilms();
    List<FilmResponseDto> getFilmsByType(FilmType type);
    FilmResponseDto createFilm(CreateFilmRequest request);
    List<FilmDetailDto> getAllFilms();
    PagedResponse<FilmDetailDto> getAllFilmsPaged(int page, int size, String search);
    FilmDetailDto getFilmById(Integer id);
    FilmDetailDto updateFilm(Integer id, UpdateFilmRequest request);
    void deleteFilm(Integer id);
}
