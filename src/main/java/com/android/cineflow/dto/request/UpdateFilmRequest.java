package com.android.cineflow.dto.request;

import com.android.cineflow.model.enums.FilmType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFilmRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String thumbnailUrl;

    private String trailerUrl;

    private Integer releaseYear;

    private Boolean isPremium;

    @NotNull(message = "Film type is required")
    private FilmType type;
}
