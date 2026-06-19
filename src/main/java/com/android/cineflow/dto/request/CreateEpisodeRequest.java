package com.android.cineflow.dto.request;

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
public class CreateEpisodeRequest {

    @NotNull(message = "Episode number is required")
    private Integer episodeNumber;

    @NotBlank(message = "Title is required")
    private String title;

    private String videoUrl;

    private Integer duration;
}
