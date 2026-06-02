package com.android.cineflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAnalyticsDto {
    private int totalWatchTimeMinutes;
    private int totalEpisodesWatched;
    private int averageWatchTimePerDay;
    private int actionPercent;
    private int animationPercent;
    private int romancePercent;
}
