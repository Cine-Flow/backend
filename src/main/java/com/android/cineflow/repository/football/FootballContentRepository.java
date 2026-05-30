package com.android.cineflow.repository.football;

import com.android.cineflow.model.football.FootballContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FootballContentRepository extends JpaRepository<FootballContent, Integer> {
    List<FootballContent> findAllByOrderByPublishedAtDesc();
    List<FootballContent> findByContentTypeOrderByPublishedAtDesc(String contentType);
}
