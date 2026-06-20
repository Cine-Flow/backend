package com.android.cineflow.repository;

import com.android.cineflow.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUserIdOrderByAddedAtDesc(String userId);
    Optional<Favorite> findByUserIdAndFilmId(String userId, Integer filmId);
    long countByUserId(String userId);
    long countByFilmId(Integer filmId);
}

