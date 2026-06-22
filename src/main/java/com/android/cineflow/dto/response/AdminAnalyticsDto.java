package com.android.cineflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class AdminAnalyticsDto {
    private int period;

    // Overview KPIs
    private long totalUsers;
    private long newSignups;
    private long activeUsers;
    private long episodeViews;
    private long watchSessions;
    private long totalFavorites;
    private long totalComments;

    // Time-series (length = period days)
    private List<TimePoint> dailySignups;
    private List<TimePoint> dailyWatchSessions;

    // Catalog
    private List<MetricSlice> filmTypes;
    private List<MetricSlice> premiumFreeMix;
    private List<MetricBar> topCategories;
    private List<MetricBar> topFilms;
    private List<MetricBar> topFavoritedFilms;
    private List<MetricBar> topCommentedFilms;
    private List<MetricBar> topEpisodes;
    private long filmsWithZeroViews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricSlice {
        private String label;
        private long value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricBar {
        private String label;
        private long value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePoint {
        private String date; // ISO yyyy-MM-dd
        private long value;
    }
}
