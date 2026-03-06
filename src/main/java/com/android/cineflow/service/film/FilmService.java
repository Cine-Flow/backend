package com.android.cineflow.service.film;

import com.android.cineflow.dto.request.CreateFilmRequest;
import com.android.cineflow.dto.response.FilmResponseDto;
import com.android.cineflow.dto.response.HomeFilmsResponse;
import com.android.cineflow.model.Film;
import com.android.cineflow.model.enums.FilmType;
import com.android.cineflow.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService implements IFilmService {

    private final FilmRepository filmRepository;
    private final ModelMapper modelMapper;

    @Override
    public HomeFilmsResponse getHomeFilms() {
        Sort byIdDesc  = Sort.by(Sort.Direction.DESC, "id");
        Sort byNewDesc = Sort.by(Sort.Direction.DESC, "releaseYear", "id");

        List<Film> banners     = filmRepository.findAll(PageRequest.of(0, 3, byIdDesc)).getContent();
        List<Film> newReleases = filmRepository.findByTypeIn(List.of(FilmType.SINGLE, FilmType.SERIES), PageRequest.of(0, 6, byNewDesc));
        List<Film> sportEvents = filmRepository.findByType(FilmType.LIVE,   PageRequest.of(0, 4, byIdDesc));
        List<Film> hotSeries   = filmRepository.findByType(FilmType.SERIES, PageRequest.of(0, 8, byIdDesc));
        List<Film> dailyMovies = filmRepository.findByType(FilmType.SINGLE, PageRequest.of(0, 6, byIdDesc));

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

    private List<FilmResponseDto> toDto(List<Film> films) {
        return films.stream()
                .map(f -> modelMapper.map(f, FilmResponseDto.class))
                .toList();
    }
}
