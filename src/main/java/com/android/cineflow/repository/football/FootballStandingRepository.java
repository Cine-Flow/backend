package com.android.cineflow.repository.football;

import com.android.cineflow.model.football.FootballStanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FootballStandingRepository extends JpaRepository<FootballStanding, Integer> {
    List<FootballStanding> findBySeasonOrderByRankAsc(String season);
}
