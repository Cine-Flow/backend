package com.android.cineflow.repository;

import com.android.cineflow.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query("select c.id, count(fc.id.categoryId) from Category c left join FilmCategory fc on fc.category = c group by c.id")
    java.util.List<Object[]> countFilmsByCategory();
}
