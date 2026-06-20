package com.android.cineflow.service.football;

import com.android.cineflow.dto.response.football.*;
import com.android.cineflow.dto.response.football.external.FootballDataResponse;
import com.android.cineflow.exceptions.ResourceNotFoundException;
import com.android.cineflow.model.Film;
import com.android.cineflow.model.enums.FilmType;
import com.android.cineflow.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PremierLeagueService implements IPremierLeagueService {

    private static final String CURRENT_SEASON = "2025/26";
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String STATUS_LIVE = "LIVE";
    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final String STATUS_FINISHED = "FINISHED";
    private static final int HOME_MATCH_PREVIEW_LIMIT = 5;
    private static final int HOME_STANDINGS_PREVIEW_LIMIT = 10;

    private final FilmRepository filmRepository;
    private final FootballDataClient footballDataClient;

    @Override
    public PremierLeagueHomeResponse getHome() {
        List<FootballMatchDto> schedule = getUpcomingMatchesFromApi(null).stream()
                .limit(HOME_MATCH_PREVIEW_LIMIT)
                .toList();

        List<FootballMatchDto> results = getFinishedMatchesFromApi(null).stream()
                .limit(HOME_MATCH_PREVIEW_LIMIT)
                .toList();

        return PremierLeagueHomeResponse.builder()
                .banners(toContentDtos(filmRepository.findByTypeAndBadge(FilmType.SPORTS, "BANNER")))
                .highlights(toContentDtos(filmRepository.findByTypeAndBadge(FilmType.SPORTS, "HIGHLIGHT")))
                .schedule(schedule)
                .results(results)
                .standings(getStandings().stream().limit(HOME_STANDINGS_PREVIEW_LIMIT).toList())
                .news(toContentDtos(filmRepository.findByTypeAndBadge(FilmType.SPORTS, "NEWS")))
                .build();
    }

    @Override
    public List<FootballMatchDto> getMatches(String status, LocalDate date) {
        if (status == null || status.isBlank()) {
            return getMatchesFromApi(date, date, null, currentPremierLeagueSeasonStartYear()).stream()
                    .sorted(java.util.Comparator.comparing(FootballMatchDto::getKickoffAt))
                    .toList();
        }

        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (STATUS_FINISHED.equals(normalizedStatus)) {
            return getFinishedMatchesFromApi(date);
        }

        if (date != null) {
            return getMatchesFromApi(date, date, null, null).stream()
                    .filter(m -> STATUS_LIVE.equals(m.getStatus()) || STATUS_SCHEDULED.equals(m.getStatus()))
                    .sorted(java.util.Comparator.comparing(FootballMatchDto::getKickoffAt))
                    .toList();
        }

        return getUpcomingMatchesFromApi(null);
    }

    @Override
    public FootballMatchDto getMatchById(Integer id) {
        List<FootballMatchDto> allMatches = getMatchesFromApi(null, null, null, null);

        return allMatches.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Football match not found with id: " + id));
    }

    @Override
    public List<FootballStandingDto> getStandings() {
        try {
            FootballDataResponse.StandingsEnvelope envelope =
                    footballDataClient.getStandingsFromApi(currentPremierLeagueSeasonStartYear());
            if (envelope != null && envelope.getStandings() != null && !envelope.getStandings().isEmpty()) {
                String seasonLabel = toSeasonLabel(envelope.getSeason());
                return envelope.getStandings().stream()
                        .filter(s -> "TOTAL".equals(s.getType()))
                        .findFirst()
                        .map(s -> s.getTable().stream()
                                .map(entry -> toStandingDto(entry, seasonLabel))
                                .toList())
                        .orElseGet(List::of);
            }
        } catch (Exception ignored) {}
        return List.of();
    }

    private FootballStandingDto toStandingDto(FootballDataResponse.ExternalTableEntry entry, String seasonLabel) {
        return FootballStandingDto.builder()
                .season(seasonLabel)
                .rank(entry.getPosition())
                .team(FootballTeamDto.builder()
                        .id(entry.getTeam() != null ? entry.getTeam().getId() : null)
                        .code(entry.getTeam() != null ? entry.getTeam().getTla() : "")
                        .name(teamName(entry.getTeam()))
                        .logoUrl(entry.getTeam() != null ? entry.getTeam().getCrest() : "")
                        .build())
                .played(entry.getPlayedGames())
                .won(entry.getWon())
                .drawn(entry.getDraw())
                .lost(entry.getLost())
                .goalDifference(entry.getGoalDifference())
                .points(entry.getPoints())
                .build();
    }

    private List<FootballMatchDto> getUpcomingMatchesFromApi(LocalDate date) {
        LocalDate today = LocalDate.now(DISPLAY_ZONE);
        LocalDate from = date != null ? date : today;
        LocalDate to = date != null ? date : today.plusYears(1);
        return getMatchesFromApi(from, to, null, null).stream()
                .filter(m -> STATUS_LIVE.equals(m.getStatus()) || STATUS_SCHEDULED.equals(m.getStatus()))
                .sorted(java.util.Comparator.comparing(FootballMatchDto::getKickoffAt))
                .toList();
    }

    private List<FootballMatchDto> getFinishedMatchesFromApi(LocalDate date) {
        LocalDate today = LocalDate.now(DISPLAY_ZONE);
        LocalDate from = date != null ? date : today.minusYears(1);
        LocalDate to = date != null ? date : today;
        return getMatchesFromApi(from, to, STATUS_FINISHED, currentPremierLeagueSeasonStartYear()).stream()
                .filter(m -> STATUS_FINISHED.equals(m.getStatus()))
                .sorted(java.util.Comparator.comparing(FootballMatchDto::getKickoffAt).reversed())
                .toList();
    }

    private List<FootballMatchDto> getMatchesFromApi(LocalDate dateFrom,
                                                     LocalDate dateTo,
                                                     String status,
                                                     Integer season) {
        try {
            FootballDataResponse.MatchesEnvelope envelope =
                    footballDataClient.getMatchesFromApi(dateFrom, dateTo, status, season);
            if (envelope != null && envelope.getMatches() != null && !envelope.getMatches().isEmpty()) {
                return envelope.getMatches().stream().map(this::toMatchDto).toList();
            }
        } catch (Exception ignored) {}
        return List.of();
    }

    private FootballMatchDto toMatchDto(FootballDataResponse.ExternalMatch match) {
        LocalDateTime kickoff = LocalDateTime.now();
        if (match.getUtcDate() != null) {
            try {
                kickoff = OffsetDateTime.parse(match.getUtcDate())
                        .atZoneSameInstant(DISPLAY_ZONE)
                        .toLocalDateTime();
            } catch (Exception ignored) {}
        }

        String status = STATUS_SCHEDULED;
        if (match.getStatus() != null) {
            String extStatus = match.getStatus().toUpperCase(Locale.ROOT);
            if ("FINISHED".equals(extStatus)) {
                status = STATUS_FINISHED;
            } else if ("LIVE".equals(extStatus) || "IN_PLAY".equals(extStatus) || "PAUSED".equals(extStatus)) {
                status = STATUS_LIVE;
            } else if ("SCHEDULED".equals(extStatus) || "TIMED".equals(extStatus)) {
                status = STATUS_SCHEDULED;
            } else {
                status = extStatus;
            }
        }

        Integer homeScore = null;
        Integer awayScore = null;
        if ((STATUS_FINISHED.equals(status) || STATUS_LIVE.equals(status))
                && match.getScore() != null && match.getScore().getFullTime() != null) {
            homeScore = match.getScore().getFullTime().getHome();
            awayScore = match.getScore().getFullTime().getAway();
        }

        String highlightUrl = null;
        if (STATUS_FINISHED.equals(status)) {
            highlightUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        }

        return FootballMatchDto.builder()
                .id(match.getId())
                .homeTeam(FootballTeamDto.builder()
                        .id(match.getHomeTeam() != null ? match.getHomeTeam().getId() : null)
                        .code(match.getHomeTeam() != null ? match.getHomeTeam().getTla() : "")
                        .name(teamName(match.getHomeTeam()))
                        .logoUrl(match.getHomeTeam() != null ? match.getHomeTeam().getCrest() : "")
                        .build())
                .awayTeam(FootballTeamDto.builder()
                        .id(match.getAwayTeam() != null ? match.getAwayTeam().getId() : null)
                        .code(match.getAwayTeam() != null ? match.getAwayTeam().getTla() : "")
                        .name(teamName(match.getAwayTeam()))
                        .logoUrl(match.getAwayTeam() != null ? match.getAwayTeam().getCrest() : "")
                        .build())
                .kickoffAt(kickoff)
                .round("Vòng " + (match.getMatchday() != null ? match.getMatchday() : ""))
                .status(status)
                .homeScore(homeScore)
                .awayScore(awayScore)
                .highlightUrl(highlightUrl)
                .build();
    }

    private int currentPremierLeagueSeasonStartYear() {
        LocalDate today = LocalDate.now(DISPLAY_ZONE);
        return today.getMonthValue() >= Month.JULY.getValue() ? today.getYear() : today.getYear() - 1;
    }

    private String toSeasonLabel(FootballDataResponse.ExternalSeason season) {
        if (season == null || season.getStartDate() == null || season.getStartDate().length() < 4) {
            return CURRENT_SEASON;
        }
        try {
            int startYear = Integer.parseInt(season.getStartDate().substring(0, 4));
            return startYear + "/" + String.format(Locale.US, "%02d", (startYear + 1) % 100);
        } catch (NumberFormatException ignored) {
            return CURRENT_SEASON;
        }
    }

    private String teamName(FootballDataResponse.ExternalTeam team) {
        if (team == null) {
            return "";
        }
        return team.getShortName() != null ? team.getShortName() : team.getName();
    }

    @Override
    public List<FootballContentDto> getContents(String type) {
        if (type == null || type.isBlank()) {
            return toContentDtos(filmRepository.findByType(FilmType.SPORTS));
        }
        String normalizedType = type.trim().toUpperCase(Locale.ROOT);
        return toContentDtos(filmRepository.findByTypeAndBadge(FilmType.SPORTS, normalizedType));
    }

    @Override
    public FootballContentDto getContentById(Integer id) {
        return filmRepository.findById(id)
                .filter(f -> f.getType() == FilmType.SPORTS)
                .map(this::toContentDto)
                .orElseThrow(() -> new ResourceNotFoundException("Football content not found with id: " + id));
    }

    private List<FootballContentDto> toContentDtos(List<Film> films) {
        return films.stream().map(this::toContentDto).toList();
    }

    private FootballContentDto toContentDto(Film film) {
        return FootballContentDto.builder()
                .id(film.getId())
                .title(film.getTitle())
                .thumbnailUrl(film.getThumbnailUrl())
                .videoUrl(film.getTrailerUrl()) // Reuses trailerUrl as videoUrl
                .contentType(film.getBadge())   // e.g. "HIGHLIGHT", "BANNER", "NEWS"
                .badge(film.getBadge())
                .publishedAt(LocalDateTime.now())
                .build();
    }

    // --- High-Fidelity Football Data Mocking ---

    private List<FootballStandingDto> getMockStandings() {
        List<FootballStandingDto> list = new ArrayList<>();
        list.add(createStanding(1, "MCI", "Man City", "https://media.api-sports.io/football/teams/50.png", 38, 28, 9, 1, 62, 93));
        list.add(createStanding(2, "ARS", "Arsenal", "https://media.api-sports.io/football/teams/42.png", 38, 27, 8, 3, 54, 89));
        list.add(createStanding(3, "LIV", "Liverpool", "https://media.api-sports.io/football/teams/40.png", 38, 24, 10, 4, 45, 82));
        list.add(createStanding(4, "AVL", "Aston Villa", "https://media.api-sports.io/football/teams/66.png", 38, 20, 8, 10, 20, 68));
        list.add(createStanding(5, "TOT", "Tottenham", "https://media.api-sports.io/football/teams/47.png", 38, 20, 6, 12, 13, 66));
        list.add(createStanding(6, "CHE", "Chelsea", "https://media.api-sports.io/football/teams/49.png", 38, 18, 9, 11, 14, 63));
        list.add(createStanding(7, "MUN", "Man United", "https://media.api-sports.io/football/teams/33.png", 38, 18, 6, 14, -1, 60));
        list.add(createStanding(8, "NEW", "Newcastle", "https://media.api-sports.io/football/teams/34.png", 38, 18, 6, 14, 23, 60));
        list.add(createStanding(9, "WHU", "West Ham", "https://media.api-sports.io/football/teams/48.png", 38, 14, 10, 14, -14, 52));
        list.add(createStanding(10, "BHA", "Brighton", "https://media.api-sports.io/football/teams/51.png", 38, 12, 12, 14, -7, 48));
        list.add(createStanding(11, "CRY", "Crystal Palace", "https://media.api-sports.io/football/teams/52.png", 38, 13, 10, 15, -1, 49));
        list.add(createStanding(12, "EVE", "Everton", "https://media.api-sports.io/football/teams/45.png", 38, 13, 9, 16, -11, 48));
        list.add(createStanding(13, "FUL", "Fulham", "https://media.api-sports.io/football/teams/36.png", 38, 13, 8, 17, -6, 47));
        list.add(createStanding(14, "WOL", "Wolves", "https://media.api-sports.io/football/teams/39.png", 38, 13, 7, 18, -15, 46));
        list.add(createStanding(15, "BOU", "Bournemouth", "https://media.api-sports.io/football/teams/35.png", 38, 13, 9, 16, -13, 48));
        list.add(createStanding(16, "BRE", "Brentford", "https://media.api-sports.io/football/teams/55.png", 38, 10, 9, 19, -9, 39));
        list.add(createStanding(17, "NFO", "Nottingham Forest", "https://media.api-sports.io/football/teams/65.png", 38, 9, 9, 20, -18, 36));
        list.add(createStanding(18, "LUT", "Luton Town", "https://media.api-sports.io/football/teams/1359.png", 38, 6, 8, 24, -33, 26));
        list.add(createStanding(19, "BUR", "Burnley", "https://media.api-sports.io/football/teams/44.png", 38, 5, 9, 24, -37, 24));
        list.add(createStanding(20, "SHU", "Sheffield Utd", "https://media.api-sports.io/football/teams/62.png", 38, 3, 7, 28, -69, 16));
        return list;
    }

    private FootballStandingDto createStanding(int rank, String code, String name, String logo, int p, int w, int d, int l, int gd, int pts) {
        return FootballStandingDto.builder()
                .season(CURRENT_SEASON)
                .rank(rank)
                .team(FootballTeamDto.builder().id(rank).code(code).name(name).logoUrl(logo).build())
                .played(p)
                .won(w)
                .drawn(d)
                .lost(l)
                .goalDifference(gd)
                .points(pts)
                .build();
    }

    private List<FootballMatchDto> getMockScheduleMatches() {
        List<FootballMatchDto> list = new ArrayList<>();
        list.add(FootballMatchDto.builder()
                .id(1001)
                .homeTeam(FootballTeamDto.builder().id(49).code("CHE").name("Chelsea").logoUrl("https://media.api-sports.io/football/teams/49.png").build())
                .awayTeam(FootballTeamDto.builder().id(33).code("MUN").name("Man United").logoUrl("https://media.api-sports.io/football/teams/33.png").build())
                .kickoffAt(LocalDateTime.now())
                .round("Vòng 38")
                .status(STATUS_LIVE)
                .build());

        list.add(FootballMatchDto.builder()
                .id(1002)
                .homeTeam(FootballTeamDto.builder().id(50).code("MCI").name("Man City").logoUrl("https://media.api-sports.io/football/teams/50.png").build())
                .awayTeam(FootballTeamDto.builder().id(42).code("ARS").name("Arsenal").logoUrl("https://media.api-sports.io/football/teams/42.png").build())
                .kickoffAt(LocalDateTime.now().plusHours(2))
                .round("Vòng 38")
                .status(STATUS_SCHEDULED)
                .build());

        list.add(FootballMatchDto.builder()
                .id(1005)
                .homeTeam(FootballTeamDto.builder().id(40).code("LIV").name("Liverpool").logoUrl("https://media.api-sports.io/football/teams/40.png").build())
                .awayTeam(FootballTeamDto.builder().id(66).code("AVL").name("Aston Villa").logoUrl("https://media.api-sports.io/football/teams/66.png").build())
                .kickoffAt(LocalDateTime.now().plusHours(4))
                .round("Round 38")
                .status(STATUS_SCHEDULED)
                .build());

        list.add(FootballMatchDto.builder()
                .id(1006)
                .homeTeam(FootballTeamDto.builder().id(47).code("TOT").name("Tottenham").logoUrl("https://media.api-sports.io/football/teams/47.png").build())
                .awayTeam(FootballTeamDto.builder().id(34).code("NEW").name("Newcastle").logoUrl("https://media.api-sports.io/football/teams/34.png").build())
                .kickoffAt(LocalDateTime.now().plusDays(1))
                .round("Round 38")
                .status(STATUS_SCHEDULED)
                .build());

        list.add(FootballMatchDto.builder()
                .id(1007)
                .homeTeam(FootballTeamDto.builder().id(51).code("BHA").name("Brighton").logoUrl("https://media.api-sports.io/football/teams/51.png").build())
                .awayTeam(FootballTeamDto.builder().id(48).code("WHU").name("West Ham").logoUrl("https://media.api-sports.io/football/teams/48.png").build())
                .kickoffAt(LocalDateTime.now().plusDays(1).plusHours(2))
                .round("Round 38")
                .status(STATUS_SCHEDULED)
                .build());
        return list;
    }

    private List<FootballMatchDto> getMockResultMatches() {
        List<FootballMatchDto> list = new ArrayList<>();
        list.add(FootballMatchDto.builder()
                .id(1003)
                .homeTeam(FootballTeamDto.builder().id(42).code("ARS").name("Arsenal").logoUrl("https://media.api-sports.io/football/teams/42.png").build())
                .awayTeam(FootballTeamDto.builder().id(49).code("CHE").name("Chelsea").logoUrl("https://media.api-sports.io/football/teams/49.png").build())
                .kickoffAt(LocalDateTime.now().minusDays(1))
                .round("Vòng 37")
                .status(STATUS_FINISHED)
                .homeScore(3)
                .awayScore(1)
                .highlightUrl("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                .build());

        list.add(FootballMatchDto.builder()
                .id(1004)
                .homeTeam(FootballTeamDto.builder().id(40).code("LIV").name("Liverpool").logoUrl("https://media.api-sports.io/football/teams/40.png").build())
                .awayTeam(FootballTeamDto.builder().id(47).code("TOT").name("Tottenham").logoUrl("https://media.api-sports.io/football/teams/47.png").build())
                .kickoffAt(LocalDateTime.now().minusDays(1))
                .round("Vòng 37")
                .status(STATUS_FINISHED)
                .homeScore(2)
                .awayScore(2)
                .highlightUrl("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                .build());

        list.add(FootballMatchDto.builder()
                .id(1008)
                .homeTeam(FootballTeamDto.builder().id(33).code("MUN").name("Man United").logoUrl("https://media.api-sports.io/football/teams/33.png").build())
                .awayTeam(FootballTeamDto.builder().id(51).code("BHA").name("Brighton").logoUrl("https://media.api-sports.io/football/teams/51.png").build())
                .kickoffAt(LocalDateTime.now().minusDays(2))
                .round("Round 37")
                .status(STATUS_FINISHED)
                .homeScore(1)
                .awayScore(0)
                .highlightUrl("https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4")
                .build());

        list.add(FootballMatchDto.builder()
                .id(1009)
                .homeTeam(FootballTeamDto.builder().id(34).code("NEW").name("Newcastle").logoUrl("https://media.api-sports.io/football/teams/34.png").build())
                .awayTeam(FootballTeamDto.builder().id(50).code("MCI").name("Man City").logoUrl("https://media.api-sports.io/football/teams/50.png").build())
                .kickoffAt(LocalDateTime.now().minusDays(2))
                .round("Round 37")
                .status(STATUS_FINISHED)
                .homeScore(1)
                .awayScore(3)
                .highlightUrl("https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
                .build());

        list.add(FootballMatchDto.builder()
                .id(1010)
                .homeTeam(FootballTeamDto.builder().id(48).code("WHU").name("West Ham").logoUrl("https://media.api-sports.io/football/teams/48.png").build())
                .awayTeam(FootballTeamDto.builder().id(66).code("AVL").name("Aston Villa").logoUrl("https://media.api-sports.io/football/teams/66.png").build())
                .kickoffAt(LocalDateTime.now().minusDays(3))
                .round("Round 36")
                .status(STATUS_FINISHED)
                .homeScore(2)
                .awayScore(1)
                .highlightUrl("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                .build());
        return list;
    }
}
