package com.android.cineflow.service.football;

import com.android.cineflow.dto.response.football.*;
import com.android.cineflow.exceptions.ResourceNotFoundException;
import com.android.cineflow.model.football.FootballContent;
import com.android.cineflow.model.football.FootballMatch;
import com.android.cineflow.model.football.FootballStanding;
import com.android.cineflow.model.football.FootballTeam;
import com.android.cineflow.repository.football.FootballContentRepository;
import com.android.cineflow.repository.football.FootballMatchRepository;
import com.android.cineflow.repository.football.FootballStandingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PremierLeagueService implements IPremierLeagueService {

    private static final String CURRENT_SEASON = "2025/26";
    private static final String STATUS_LIVE = "LIVE";
    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final String STATUS_FINISHED = "FINISHED";
    private static final String CONTENT_BANNER = "BANNER";
    private static final String CONTENT_HIGHLIGHT = "HIGHLIGHT";
    private static final String CONTENT_NEWS = "NEWS";
    private static final int HOME_MATCH_PREVIEW_LIMIT = 5;
    private static final int HOME_STANDINGS_PREVIEW_LIMIT = 10;
    private static final Set<String> MATCH_STATUSES = Set.of(STATUS_LIVE, STATUS_SCHEDULED, STATUS_FINISHED);
    private static final Set<String> CONTENT_TYPES = Set.of(CONTENT_BANNER, CONTENT_HIGHLIGHT, CONTENT_NEWS);

    private final FootballMatchRepository matchRepository;
    private final FootballStandingRepository standingRepository;
    private final FootballContentRepository contentRepository;

    @Override
    public PremierLeagueHomeResponse getHome() {
        return PremierLeagueHomeResponse.builder()
                .banners(toContentDtos(contentRepository.findByContentTypeOrderByPublishedAtDesc(CONTENT_BANNER)))
                .highlights(toContentDtos(contentRepository.findByContentTypeOrderByPublishedAtDesc(CONTENT_HIGHLIGHT)))
                .schedule(toMatchDtos(matchRepository.findByStatusInOrderByKickoffAtAsc(
                                List.of(STATUS_LIVE, STATUS_SCHEDULED))
                        .stream()
                        .limit(HOME_MATCH_PREVIEW_LIMIT)
                        .toList()))
                .results(toMatchDtos(matchRepository.findByStatusOrderByKickoffAtDesc(STATUS_FINISHED)
                        .stream()
                        .limit(HOME_MATCH_PREVIEW_LIMIT)
                        .toList()))
                .standings(standingRepository.findBySeasonOrderByRankAsc(CURRENT_SEASON)
                        .stream()
                        .limit(HOME_STANDINGS_PREVIEW_LIMIT)
                        .map(this::toStandingDto)
                        .toList())
                .news(toContentDtos(contentRepository.findByContentTypeOrderByPublishedAtDesc(CONTENT_NEWS)))
                .build();
    }

    @Override
    public List<FootballMatchDto> getMatches(String status) {
        if (status == null || status.isBlank()) {
            return toMatchDtos(matchRepository.findAllByOrderByKickoffAtDesc());
        }

        String normalizedStatus = normalizeFilter(status);
        if (!MATCH_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Unsupported football match status: " + status);
        }
        return toMatchDtos(matchRepository.findByStatusOrderByKickoffAtDesc(normalizedStatus));
    }

    @Override
    public FootballMatchDto getMatchById(Integer id) {
        return matchRepository.findById(id)
                .map(this::toMatchDto)
                .orElseThrow(() -> new ResourceNotFoundException("Football match not found with id: " + id));
    }

    @Override
    public List<FootballStandingDto> getStandings() {
        return standingRepository.findBySeasonOrderByRankAsc(CURRENT_SEASON)
                .stream()
                .map(this::toStandingDto)
                .toList();
    }

    @Override
    public List<FootballContentDto> getContents(String type) {
        if (type == null || type.isBlank()) {
            return toContentDtos(contentRepository.findAllByOrderByPublishedAtDesc());
        }

        String normalizedType = normalizeFilter(type);
        if (!CONTENT_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("Unsupported football content type: " + type);
        }
        return toContentDtos(contentRepository.findByContentTypeOrderByPublishedAtDesc(normalizedType));
    }

    @Override
    public FootballContentDto getContentById(Integer id) {
        return contentRepository.findById(id)
                .map(this::toContentDto)
                .orElseThrow(() -> new ResourceNotFoundException("Football content not found with id: " + id));
    }

    private List<FootballMatchDto> toMatchDtos(List<FootballMatch> matches) {
        return matches.stream().map(this::toMatchDto).toList();
    }

    private List<FootballContentDto> toContentDtos(List<FootballContent> contents) {
        return contents.stream().map(this::toContentDto).toList();
    }

    private FootballTeamDto toTeamDto(FootballTeam team) {
        return FootballTeamDto.builder()
                .id(team.getId())
                .code(team.getCode())
                .name(team.getName())
                .logoUrl(team.getLogoUrl())
                .build();
    }

    private FootballMatchDto toMatchDto(FootballMatch match) {
        return FootballMatchDto.builder()
                .id(match.getId())
                .homeTeam(toTeamDto(match.getHomeTeam()))
                .awayTeam(toTeamDto(match.getAwayTeam()))
                .kickoffAt(match.getKickoffAt())
                .round(match.getRound())
                .status(match.getStatus())
                .homeScore(match.getHomeScore())
                .awayScore(match.getAwayScore())
                .bannerUrl(match.getBannerUrl())
                .highlightUrl(match.getHighlightUrl())
                .build();
    }

    private FootballStandingDto toStandingDto(FootballStanding standing) {
        return FootballStandingDto.builder()
                .team(toTeamDto(standing.getTeam()))
                .season(standing.getSeason())
                .rank(standing.getRank())
                .played(standing.getPlayed())
                .won(standing.getWon())
                .drawn(standing.getDrawn())
                .lost(standing.getLost())
                .goalDifference(standing.getGoalDifference())
                .points(standing.getPoints())
                .build();
    }

    private FootballContentDto toContentDto(FootballContent content) {
        return FootballContentDto.builder()
                .id(content.getId())
                .title(content.getTitle())
                .thumbnailUrl(content.getThumbnailUrl())
                .videoUrl(content.getVideoUrl())
                .contentType(content.getContentType())
                .badge(content.getBadge())
                .publishedAt(content.getPublishedAt())
                .build();
    }

    private String normalizeFilter(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
