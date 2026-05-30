package com.android.cineflow.model.football;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "football_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;
}
