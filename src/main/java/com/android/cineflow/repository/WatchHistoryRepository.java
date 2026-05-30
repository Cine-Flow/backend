package com.android.cineflow.repository;

import com.android.cineflow.model.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Integer> {
    List<WatchHistory> findByUserIdOrderByLastWatchedAtDesc(String userId);
    Optional<WatchHistory> findByUserIdAndEpisodeId(String userId, Integer episodeId);
    long countByUserId(String userId);
}
