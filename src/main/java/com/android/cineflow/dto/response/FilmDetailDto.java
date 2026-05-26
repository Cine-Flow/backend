package com.android.cineflow.dto.response;

import com.android.cineflow.model.enums.FilmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmDetailDto {
    private Integer id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String trailerUrl;
    private Integer releaseYear;
    private Boolean isPremium;
    private FilmType type;
    private Instant createdAt;
    private Instant updatedAt;
    private List<EpisodeDto> episodes;
}
