package com.android.cineflow.model.football;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "football_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FootballContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    private String badge;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;
}
