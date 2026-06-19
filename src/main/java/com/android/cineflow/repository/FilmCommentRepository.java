package com.android.cineflow.repository;

import com.android.cineflow.model.FilmComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilmCommentRepository extends JpaRepository<FilmComment, Integer> {
    List<FilmComment> findByFilmIdOrderByCreatedAtDesc(Integer filmId);
    long countByFilmId(Integer filmId);
}
