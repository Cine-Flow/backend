package com.android.cineflow.repository;

import com.android.cineflow.model.Film;
import com.android.cineflow.model.enums.FilmType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilmRepository extends JpaRepository<Film, Integer> {
    List<Film> findByTypeIn(List<FilmType> types, Pageable pageable);
    List<Film> findByType(FilmType type, Pageable pageable);

    @Query("SELECT f FROM Film f WHERE " +
           "LOWER(f.title) LIKE :keyword OR " +
           "LOWER(CAST(f.type AS string)) LIKE :keyword OR " +
           "CAST(f.releaseYear AS string) LIKE :keyword")
    Page<Film> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
