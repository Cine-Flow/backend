package com.android.cineflow.service.shorts;

import com.android.cineflow.dto.response.ShortsDto;
import com.android.cineflow.dto.response.ShortsResponse;
import com.android.cineflow.model.Episode;
import com.android.cineflow.repository.EpisodeRepository;
import com.android.cineflow.repository.FavoriteRepository;
import com.android.cineflow.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShortsService implements IShortsService {

    private final EpisodeRepository episodeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public ShortsResponse getShorts() {
        Sort sort = Sort.by(Sort.Direction.DESC, "viewCount");
        List<Episode> episodes = episodeRepository.findAll(sort);

        String currentUserId = null;
        try {
            currentUserId = currentUserService.getCurrentUser().getId();
        } catch (Exception e) {
            // User not logged in, ignore
        }

        final List<Integer> favoriteFilmIds;
        if (currentUserId != null) {
            favoriteFilmIds = favoriteRepository.findByUserIdOrderByAddedAtDesc(currentUserId)
                    .stream()
                    .map(fav -> fav.getFilm().getId())
                    .toList();
        } else {
            favoriteFilmIds = new ArrayList<>();
        }

        List<ShortsDto> shorts = episodes.stream()
                .filter(ep -> ep.getVideoUrl() != null && !ep.getVideoUrl().isEmpty())
                .map(ep -> {
                    Integer filmId = ep.getFilm().getId();
                    boolean isLiked = favoriteFilmIds.contains(filmId);
                    int likeCount = (int) favoriteRepository.countByFilmId(filmId);

                    return ShortsDto.builder()
                            .id(filmId)
                            .title(ep.getFilm().getTitle())
                            .thumbnailUrl(ep.getFilm().getThumbnailUrl())
                            .videoUrl(ep.getVideoUrl())
                            .description(ep.getFilm().getDescription())
                            .uploader(ep.getFilm().getTitle())
                            .duration(ep.getDuration())
                            .viewCount(ep.getViewCount())
                            .likeCount(likeCount)
                            .liked(isLiked)
                            .build();
                })
                .toList();

        return ShortsResponse.builder().shorts(shorts).build();
    }
}


