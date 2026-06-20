package com.android.cineflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCategoryDto {
    private Integer id;
    private String name;
    private String description;
    private long filmCount;
}
