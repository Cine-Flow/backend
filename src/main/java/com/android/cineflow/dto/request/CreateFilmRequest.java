package com.android.cineflow.dto.request;

import com.android.cineflow.model.enums.FilmType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateFilmRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String thumbnailUrl;

    private String trailerUrl;

    private Integer releaseYear;

    private Boolean isPremium = false;

    @NotNull(message = "Film type is required")
    private FilmType type;
}
