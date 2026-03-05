package com.android.cineflow.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "episodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "film_id", nullable = false)
    private Film film;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "video_url")
    private String videoUrl;

    /**
     * Duration in seconds (Tính bằng giây)
     */
    private Integer duration;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;
}
