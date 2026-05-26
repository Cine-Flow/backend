package com.android.cineflow.service.film;

import com.android.cineflow.dto.request.CreateFilmRequest;
import com.android.cineflow.dto.request.UpdateFilmRequest;
import com.android.cineflow.dto.response.FilmDetailDto;
import com.android.cineflow.dto.response.FilmResponseDto;
import com.android.cineflow.dto.response.HomeFilmsResponse;
import com.android.cineflow.exceptions.ResourceNotFoundException;
import com.android.cineflow.model.Film;
import com.android.cineflow.model.enums.FilmType;
import com.android.cineflow.repository.EpisodeRepository;
import com.android.cineflow.model.Episode;
import com.android.cineflow.dto.response.EpisodeDto;
import com.android.cineflow.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService implements IFilmService {

    private final FilmRepository filmRepository;
    private final EpisodeRepository episodeRepository;
    private final ModelMapper modelMapper;

    @Override
    public HomeFilmsResponse getHomeFilms() {
        Sort byIdDesc  = Sort.by(Sort.Direction.DESC, "id");
        Sort byNewDesc = Sort.by(Sort.Direction.DESC, "releaseYear", "id");

        // NOTE: DB uses PostgreSQL native enum type for films.type.
        // To keep home API resilient across enum JDBC binding differences,
        // fetch + filter in memory for these small home sections.
        List<Film> allByIdDesc = filmRepository.findAll(byIdDesc);
        List<Film> allByNewDesc = filmRepository.findAll(byNewDesc);

        List<Film> banners = allByIdDesc.stream()
                .limit(3)
                .toList();

        List<Film> newReleases = allByNewDesc.stream()
                .filter(film -> film.getType() == FilmType.SINGLE || film.getType() == FilmType.SERIES)
                .limit(6)
                .toList();

        List<Film> sportEvents = allByIdDesc.stream()
                .filter(film -> film.getType() == FilmType.LIVE)
                .limit(4)
                .toList();

        List<Film> hotSeries = allByIdDesc.stream()
                .filter(film -> film.getType() == FilmType.SERIES)
                .limit(8)
                .toList();

        List<Film> dailyMovies = allByIdDesc.stream()
                .filter(film -> film.getType() == FilmType.SINGLE)
                .limit(6)
                .toList();

        return HomeFilmsResponse.builder()
                .banners(toDto(banners))
                .newReleases(toDto(newReleases))
                .sportEvents(toDto(sportEvents))
                .hotSeries(toDto(hotSeries))
                .dailyMovies(toDto(dailyMovies))
                .build();
    }

    @Override
    public FilmResponseDto createFilm(CreateFilmRequest request) {
        Film film = modelMapper.map(request, Film.class);
        return modelMapper.map(filmRepository.save(film), FilmResponseDto.class);
    }

    @Override
    public List<FilmDetailDto> getAllFilms() {
        return filmRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::toDetailDto)
                .toList();
    }

    @Override
    public FilmDetailDto getFilmById(Integer id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Film not found with id: " + id));
        return toDetailDto(film);
    }

    @Override
    @Transactional
    public FilmDetailDto updateFilm(Integer id, UpdateFilmRequest request) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Film not found with id: " + id));

        film.setTitle(request.getTitle());
        film.setDescription(request.getDescription());
        film.setThumbnailUrl(request.getThumbnailUrl());
        film.setTrailerUrl(request.getTrailerUrl());
        film.setReleaseYear(request.getReleaseYear());
        film.setIsPremium(request.getIsPremium());
        film.setType(request.getType());

        return toDetailDto(filmRepository.save(film));
    }

    @Override
    @Transactional
    public void deleteFilm(Integer id) {
        if (!filmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Film not found with id: " + id);
        }
        filmRepository.deleteById(id);
    }

    private List<FilmResponseDto> toDto(List<Film> films) {
        return films.stream()
                .map(f -> modelMapper.map(f, FilmResponseDto.class))
                .toList();
    }

    private FilmDetailDto toDetailDto(Film film) {
        FilmDetailDto dto = modelMapper.map(film, FilmDetailDto.class);
        List<Episode> episodes = episodeRepository.findByFilmIdOrderByEpisodeNumberAsc(film.getId());
        List<EpisodeDto> episodeDtos = episodes.stream()
                .map(e -> modelMapper.map(e, EpisodeDto.class))
                .toList();
        dto.setEpisodes(episodeDtos);
        return dto;
    }
}
