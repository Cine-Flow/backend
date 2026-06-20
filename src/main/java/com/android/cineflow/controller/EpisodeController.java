package com.android.cineflow.controller;

import com.android.cineflow.dto.request.CreateEpisodeRequest;
import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.EpisodeDto;
import com.android.cineflow.exceptions.ResourceNotFoundException;
import com.android.cineflow.model.Episode;
import com.android.cineflow.model.Film;
import com.android.cineflow.model.enums.FilmType;
import com.android.cineflow.repository.EpisodeRepository;
import com.android.cineflow.repository.FilmRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeRepository episodeRepository;
    private final FilmRepository filmRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/films/{filmId}/episodes")
    public ResponseEntity<ApiResponse<List<EpisodeDto>>> getEpisodes(@PathVariable Integer filmId) {
        List<Episode> episodes = episodeRepository.findByFilmIdOrderByEpisodeNumberAsc(filmId);
        List<EpisodeDto> dtos = episodes.stream()
                .map(e -> modelMapper.map(e, EpisodeDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Episodes fetched", dtos));
    }

    @PostMapping("/films/{filmId}/episodes")
    public ResponseEntity<ApiResponse<EpisodeDto>> createEpisode(
            @PathVariable Integer filmId,
            @Valid @RequestBody CreateEpisodeRequest request) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new ResourceNotFoundException("Film not found with id: " + filmId));
        if (film.getType() != FilmType.SERIES) {
            throw new IllegalArgumentException("Only SERIES films can have manually managed episodes.");
        }
        if (episodeRepository.existsByFilmIdAndEpisodeNumber(filmId, request.getEpisodeNumber())) {
            throw new IllegalArgumentException("Episode number already exists for this film.");
        }

        Episode episode = Episode.builder()
                .film(film)
                .episodeNumber(request.getEpisodeNumber())
                .title(request.getTitle())
                .videoUrl(request.getVideoUrl())
                .duration(request.getDuration() != null ? request.getDuration() : 2700)
                .viewCount(0)
                .build();

        Episode saved = episodeRepository.save(episode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Episode created", modelMapper.map(saved, EpisodeDto.class)));
    }

    @PutMapping("/episodes/{id}")
    public ResponseEntity<ApiResponse<EpisodeDto>> updateEpisode(
            @PathVariable Integer id,
            @Valid @RequestBody CreateEpisodeRequest request) {
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found with id: " + id));
        Integer filmId = episode.getFilm().getId();
        if (episodeRepository.existsByFilmIdAndEpisodeNumberAndIdNot(filmId, request.getEpisodeNumber(), id)) {
            throw new IllegalArgumentException("Episode number already exists for this film.");
        }

        episode.setEpisodeNumber(request.getEpisodeNumber());
        episode.setTitle(request.getTitle());
        episode.setVideoUrl(request.getVideoUrl());
        if (request.getDuration() != null) {
            episode.setDuration(request.getDuration());
        }

        Episode saved = episodeRepository.save(episode);
        return ResponseEntity.ok(ApiResponse.success("Episode updated", modelMapper.map(saved, EpisodeDto.class)));
    }

    @DeleteMapping("/episodes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEpisode(@PathVariable Integer id) {
        if (!episodeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Episode not found with id: " + id);
        }
        episodeRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Episode deleted"));
    }
}
