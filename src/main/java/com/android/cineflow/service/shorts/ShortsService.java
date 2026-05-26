package com.android.cineflow.service.shorts;

import com.android.cineflow.dto.response.ShortsDto;
import com.android.cineflow.dto.response.ShortsResponse;
import com.android.cineflow.model.Episode;
import com.android.cineflow.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShortsService implements IShortsService {

    private final EpisodeRepository episodeRepository;

    @Override
    public ShortsResponse getShorts() {
        Sort sort = Sort.by(Sort.Direction.DESC, "viewCount");
        List<Episode> episodes = episodeRepository.findAll(sort);

        List<ShortsDto> shorts = episodes.stream()
                .map(ep -> ShortsDto.builder()
                        .id(ep.getFilm().getId())
                        .title(ep.getFilm().getTitle())
                        .thumbnailUrl(ep.getFilm().getThumbnailUrl())
                        .duration(ep.getDuration())
                        .viewCount(ep.getViewCount())
                        .build())
                .toList();

        return ShortsResponse.builder().shorts(shorts).build();
    }
}

