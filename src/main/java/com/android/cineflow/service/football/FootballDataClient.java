package com.android.cineflow.service.football;

import com.android.cineflow.dto.response.football.external.FootballDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class FootballDataClient {

    private final RestTemplate restTemplate;

    @Value("${football.api.url}")
    private String apiUrl;

    @Value("${football.api.token}")
    private String apiToken;

    @org.springframework.cache.annotation.Cacheable(value = "footballStandingsRaw")
    public FootballDataResponse.StandingsEnvelope getStandingsFromApi(Integer season) {
        if (apiToken == null || apiToken.isBlank()) {
            log.warn("Football API Token is empty. Skipping API call.");
            return null;
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(apiUrl + "/v4/competitions/PL/standings");
            if (season != null) {
                builder.queryParam("season", season);
            }
            String url = builder.toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", apiToken);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            log.info("Calling Football API: {}", url);
            ResponseEntity<FootballDataResponse.StandingsEnvelope> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, FootballDataResponse.StandingsEnvelope.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch standings from Football API: {}", e.getMessage());
            return null;
        }
    }

    @org.springframework.cache.annotation.Cacheable(value = "footballMatchesRaw")
    public FootballDataResponse.MatchesEnvelope getMatchesFromApi(LocalDate dateFrom,
                                                                  LocalDate dateTo,
                                                                  String status,
                                                                  Integer season) {
        if (apiToken == null || apiToken.isBlank()) {
            log.warn("Football API Token is empty. Skipping API call.");
            return null;
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(apiUrl + "/v4/competitions/PL/matches");
            if (dateFrom != null) {
                builder.queryParam("dateFrom", dateFrom);
            }
            if (dateTo != null) {
                builder.queryParam("dateTo", dateTo);
            }
            if (status != null && !status.isBlank()) {
                builder.queryParam("status", status);
            }
            if (season != null) {
                builder.queryParam("season", season);
            }
            String url = builder.toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", apiToken);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            log.info("Calling Football API: {}", url);
            ResponseEntity<FootballDataResponse.MatchesEnvelope> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, FootballDataResponse.MatchesEnvelope.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch matches from Football API: {}", e.getMessage());
            return null;
        }
    }
}
