package com.android.cineflow.controller;

import com.android.cineflow.dto.request.CreateFilmRequest;
import com.android.cineflow.dto.request.UpdateFilmRequest;
import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.FilmDetailDto;
import com.android.cineflow.dto.response.FilmResponseDto;
import com.android.cineflow.dto.response.HomeFilmsResponse;
import com.android.cineflow.service.film.IFilmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class FilmController {

    private final IFilmService filmService;

    @GetMapping("/films/home")
    public ResponseEntity<ApiResponse<HomeFilmsResponse>> getHomeFilms() {
        return ResponseEntity.ok(ApiResponse.success("Home films fetched", filmService.getHomeFilms()));
    }

    @GetMapping("/films/{id}")
    public ResponseEntity<ApiResponse<FilmDetailDto>> getFilmById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Film fetched", filmService.getFilmById(id)));
    }

    // ─── Admin ───────────────────────────────────────────────────────────────────

    @GetMapping("/admin/films")
    public ResponseEntity<ApiResponse<List<FilmDetailDto>>> getAllFilms() {
        return ResponseEntity.ok(ApiResponse.success("Films fetched", filmService.getAllFilms()));
    }

    @GetMapping("/admin/films/{id}")
    public ResponseEntity<ApiResponse<FilmDetailDto>> getAdminFilmById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Film fetched", filmService.getFilmById(id)));
    }

    @PostMapping("/admin/films")
    public ResponseEntity<ApiResponse<FilmResponseDto>> createFilm(
            @Valid @RequestBody CreateFilmRequest request) {
        FilmResponseDto created = filmService.createFilm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Film created successfully", created));
    }

    @PutMapping("/admin/films/{id}")
    public ResponseEntity<ApiResponse<FilmDetailDto>> updateFilm(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateFilmRequest request) {
        FilmDetailDto updated = filmService.updateFilm(id, request);
        return ResponseEntity.ok(ApiResponse.success("Film updated successfully", updated));
    }

    @DeleteMapping("/admin/films/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFilm(@PathVariable Integer id) {
        filmService.deleteFilm(id);
        return ResponseEntity.ok(ApiResponse.success("Film deleted successfully"));
    }
}
