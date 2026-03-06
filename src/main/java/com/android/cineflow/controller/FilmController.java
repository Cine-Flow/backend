package com.android.cineflow.controller;

import com.android.cineflow.dto.request.CreateFilmRequest;
import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.FilmResponseDto;
import com.android.cineflow.dto.response.HomeFilmsResponse;
import com.android.cineflow.service.film.IFilmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class FilmController {

    private final IFilmService filmService;

    @GetMapping("/films/home")
    public ResponseEntity<ApiResponse<HomeFilmsResponse>> getHomeFilms() {
        return ResponseEntity.ok(ApiResponse.success("Home films fetched", filmService.getHomeFilms()));
    }

    @PostMapping("/admin/films")
    public ResponseEntity<ApiResponse<FilmResponseDto>> createFilm(
            @Valid @RequestBody CreateFilmRequest request) {
        FilmResponseDto created = filmService.createFilm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Film created successfully", created));
    }
}
