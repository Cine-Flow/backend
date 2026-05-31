package com.android.cineflow.repository.football;

import com.android.cineflow.model.football.FootballMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface FootballMatchRepository extends JpaRepository<FootballMatch, Integer> {
    List<FootballMatch> findAllByOrderByKickoffAtDesc();
    List<FootballMatch> findByStatusInOrderByKickoffAtAsc(Collection<String> statuses);
    List<FootballMatch> findByStatusOrderByKickoffAtDesc(String status);
    List<FootballMatch> findByStatusInAndKickoffAtBetweenOrderByKickoffAtAsc(
            Collection<String> statuses, LocalDateTime start, LocalDateTime end);
    List<FootballMatch> findByStatusAndKickoffAtBetweenOrderByKickoffAtDesc(
            String status, LocalDateTime start, LocalDateTime end);
}
