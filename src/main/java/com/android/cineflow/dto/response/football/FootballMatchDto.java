package com.android.cineflow.dto.response.football;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballMatchDto {
    private Integer id;
    private FootballTeamDto homeTeam;
    private FootballTeamDto awayTeam;
    private LocalDateTime kickoffAt;
    private String round;
    private String status;
    private Integer homeScore;
    private Integer awayScore;
    private String bannerUrl;
    private String highlightUrl;
}
