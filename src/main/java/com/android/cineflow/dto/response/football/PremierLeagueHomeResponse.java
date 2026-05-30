package com.android.cineflow.dto.response.football;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremierLeagueHomeResponse {
    private List<FootballContentDto> banners;
    private List<FootballContentDto> highlights;
    private List<FootballMatchDto> schedule;
    private List<FootballMatchDto> results;
    private List<FootballStandingDto> standings;
    private List<FootballContentDto> news;
}
