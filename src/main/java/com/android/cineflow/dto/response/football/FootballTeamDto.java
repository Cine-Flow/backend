package com.android.cineflow.dto.response.football;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballTeamDto {
    private Integer id;
    private String code;
    private String name;
    private String logoUrl;
}
