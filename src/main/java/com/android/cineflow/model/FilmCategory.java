package com.android.cineflow.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "film_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilmCategory {

    @EmbeddedId
    private FilmCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("filmId")
    @JoinColumn(name = "film_id")
    private Film film;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;
}
