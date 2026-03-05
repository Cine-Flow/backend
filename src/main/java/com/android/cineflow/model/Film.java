package com.android.cineflow.model;

import com.android.cineflow.model.enums.FilmType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "films")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "trailer_url")
    private String trailerUrl;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FilmType type;
}
