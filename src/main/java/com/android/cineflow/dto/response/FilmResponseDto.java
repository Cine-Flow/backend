package com.android.cineflow.dto.response;

import com.android.cineflow.model.enums.FilmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmResponseDto {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private Integer releaseYear;
    private Boolean isPremium;
    private FilmType type;
}
