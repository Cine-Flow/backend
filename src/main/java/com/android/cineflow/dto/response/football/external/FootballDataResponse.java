package com.android.cineflow.dto.response.football.external;

import lombok.Data;
import java.util.List;

public class FootballDataResponse {

    @Data
    public static class StandingsEnvelope {
        private ExternalSeason season;
        private List<ExternalStanding> standings;
    }

    @Data
    public static class ExternalStanding {
        private String stage;
        private String type;
        private List<ExternalTableEntry> table;
    }

    @Data
    public static class ExternalTableEntry {
        private Integer position;
        private ExternalTeam team;
        private Integer playedGames;
        private Integer won;
        private Integer draw;
        private Integer lost;
        private Integer points;
        private Integer goalDifference;
    }

    @Data
    public static class MatchesEnvelope {
        private ExternalSeason season;
        private List<ExternalMatch> matches;
    }

    @Data
    public static class ExternalSeason {
        private Integer id;
        private String startDate;
        private String endDate;
        private Integer currentMatchday;
    }

    @Data
    public static class ExternalMatch {
        private Integer id;
        private String utcDate;
        private String status;
        private Integer matchday;
        private String stage;
        private ExternalTeam homeTeam;
        private ExternalTeam awayTeam;
        private ExternalScore score;
    }

    @Data
    public static class ExternalTeam {
        private Integer id;
        private String name;
        private String shortName;
        private String tla;
        private String crest;
    }

    @Data
    public static class ExternalScore {
        private String winner;
        private String duration;
        private ExternalTimeScore fullTime;
    }

    @Data
    public static class ExternalTimeScore {
        private Integer home;
        private Integer away;
    }
}
