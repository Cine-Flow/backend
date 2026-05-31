package com.android.cineflow.service.football;

import com.android.cineflow.dto.response.football.FootballContentDto;
import com.android.cineflow.dto.response.football.FootballMatchDto;
import com.android.cineflow.dto.response.football.FootballStandingDto;
import com.android.cineflow.dto.response.football.PremierLeagueHomeResponse;

import java.util.List;
import java.time.LocalDate;

public interface IPremierLeagueService {
    PremierLeagueHomeResponse getHome();
    List<FootballMatchDto> getMatches(String status, LocalDate date);
    FootballMatchDto getMatchById(Integer id);
    List<FootballStandingDto> getStandings();
    List<FootballContentDto> getContents(String type);
    FootballContentDto getContentById(Integer id);
}
