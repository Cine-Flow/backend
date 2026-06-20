package com.android.cineflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminAnalyticsDto {
    private int period;
    private long totalUsers;
    private long newSignups;
    private long episodeViews;
    private long watchSessions;
    private long premiumUsers;
    private BigDecimal revenue;
    private List<MetricSlice> filmTypes;
    private List<MetricSlice> subscriptions;
    private List<MetricBar> topCategories;
    private List<MetricBar> topFilms;

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
}
