package com.android.cineflow.dto.response.football;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballStandingDto {
    private FootballTeamDto team;
    private String season;
    private Integer rank;
    private Integer played;
    private Integer won;
    private Integer drawn;
    private Integer lost;
    private Integer goalDifference;
    private Integer points;
}
