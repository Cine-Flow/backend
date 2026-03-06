package com.android.cineflow.repository;

import com.android.cineflow.model.Film;
import com.android.cineflow.model.enums.FilmType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilmRepository extends JpaRepository<Film, Integer> {
    List<Film> findByTypeIn(List<FilmType> types, Pageable pageable);
    List<Film> findByType(FilmType type, Pageable pageable);
}
