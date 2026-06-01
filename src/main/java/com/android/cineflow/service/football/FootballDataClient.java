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
import org.springframework.web.client.RestTemplate;

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
    public FootballDataResponse.StandingsEnvelope getStandingsFromApi() {
        if (apiToken == null || apiToken.isBlank()) {
            log.warn("Football API Token is empty. Skipping API call.");
            return null;
        }

        try {
            String url = apiUrl + "/v4/competitions/PL/standings";
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
    public FootballDataResponse.MatchesEnvelope getMatchesFromApi() {
        if (apiToken == null || apiToken.isBlank()) {
            log.warn("Football API Token is empty. Skipping API call.");
            return null;
        }

        try {
            String url = apiUrl + "/v4/competitions/PL/matches";
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
