package com.android.cineflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWatchHistoryRequest {
    @NotNull
    @Min(0)
    private Integer resumePositionSeconds;
}
