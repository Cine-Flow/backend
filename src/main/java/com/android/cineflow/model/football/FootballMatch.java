package com.android.cineflow.model.football;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "football_matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private FootballTeam homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private FootballTeam awayTeam;

    @Column(name = "kickoff_at", nullable = false)
    private LocalDateTime kickoffAt;

    @Column(nullable = false)
    private String round;

    @Column(nullable = false)
    private String status;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "highlight_url")
    private String highlightUrl;
}
