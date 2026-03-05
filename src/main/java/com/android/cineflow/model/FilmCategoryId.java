package com.android.cineflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FilmCategoryId implements Serializable {

    @Column(name = "film_id")
    private Integer filmId;

    @Column(name = "category_id")
    private Integer categoryId;
}
