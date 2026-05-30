package com.android.cineflow.service.football;

import com.android.cineflow.dto.response.football.FootballContentDto;
import com.android.cineflow.dto.response.football.FootballMatchDto;
import com.android.cineflow.dto.response.football.PremierLeagueHomeResponse;

public interface IPremierLeagueService {
    PremierLeagueHomeResponse getHome();
    FootballMatchDto getMatchById(Integer id);
    FootballContentDto getContentById(Integer id);
}
