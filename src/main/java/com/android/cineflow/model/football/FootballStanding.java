package com.android.cineflow.model.football;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "football_standings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballStanding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private FootballTeam team;

    @Column(nullable = false)
    private String season;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private Integer played;

    @Column(nullable = false)
    private Integer won;

    @Column(nullable = false)
    private Integer drawn;

    @Column(nullable = false)
    private Integer lost;

    @Column(name = "goal_difference", nullable = false)
    private Integer goalDifference;

    @Column(nullable = false)
    private Integer points;
}
