package com.android.cineflow.controller;

import com.android.cineflow.dto.response.ApiResponse;
import com.android.cineflow.dto.response.football.FootballContentDto;
import com.android.cineflow.dto.response.football.FootballMatchDto;
import com.android.cineflow.dto.response.football.FootballStandingDto;
import com.android.cineflow.dto.response.football.PremierLeagueHomeResponse;
import com.android.cineflow.service.football.IPremierLeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/premier-league")
@RequiredArgsConstructor
public class PremierLeagueController {

    private final IPremierLeagueService premierLeagueService;

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<PremierLeagueHomeResponse>> getHome() {
        return ResponseEntity.ok(ApiResponse.success("Premier League home fetched", premierLeagueService.getHome()));
    }

    @GetMapping("/matches")
    public ResponseEntity<ApiResponse<List<FootballMatchDto>>> getMatches(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success("Football matches fetched",
                premierLeagueService.getMatches(status)));
    }

    @GetMapping("/matches/{id}")
    public ResponseEntity<ApiResponse<FootballMatchDto>> getMatchById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Football match fetched", premierLeagueService.getMatchById(id)));
    }

    @GetMapping("/standings")
    public ResponseEntity<ApiResponse<List<FootballStandingDto>>> getStandings() {
        return ResponseEntity.ok(ApiResponse.success("Football standings fetched",
                premierLeagueService.getStandings()));
    }

    @GetMapping("/contents")
    public ResponseEntity<ApiResponse<List<FootballContentDto>>> getContents(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(ApiResponse.success("Football contents fetched",
                premierLeagueService.getContents(type)));
    }

    @GetMapping("/contents/{id}")
    public ResponseEntity<ApiResponse<FootballContentDto>> getContentById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Football content fetched", premierLeagueService.getContentById(id)));
    }
}
