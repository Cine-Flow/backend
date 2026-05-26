package com.android.cineflow.repository;

import com.android.cineflow.model.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Integer> {
    List<Episode> findByFilmIdOrderByEpisodeNumberAsc(Integer filmId);
}

