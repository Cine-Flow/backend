package com.android.cineflow.service.shorts;

import com.android.cineflow.dto.response.ShortsDto;
import com.android.cineflow.dto.response.ShortsResponse;
import com.android.cineflow.model.Episode;
import com.android.cineflow.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShortsService implements IShortsService {

    private final EpisodeRepository episodeRepository;

    @Override
    @Transactional(readOnly = true)
    public ShortsResponse getShorts() {
        Sort sort = Sort.by(Sort.Direction.DESC, "viewCount");
        List<Episode> episodes = episodeRepository.findAll(sort);

        List<ShortsDto> shorts = episodes.stream()
                .filter(ep -> ep.getVideoUrl() != null && !ep.getVideoUrl().isEmpty())
                .map(ep -> ShortsDto.builder()
                        .id(ep.getFilm().getId())
                        .title(ep.getFilm().getTitle())
                        .thumbnailUrl(ep.getFilm().getThumbnailUrl())
                        .videoUrl(ep.getVideoUrl())
                        .description(ep.getFilm().getDescription())
                        .uploader(ep.getFilm().getTitle())
                        .duration(ep.getDuration())
                        .viewCount(ep.getViewCount())
                        .build())
                .toList();

        return ShortsResponse.builder().shorts(shorts).build();
    }
}

