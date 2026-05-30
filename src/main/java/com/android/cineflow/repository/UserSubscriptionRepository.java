package com.android.cineflow.repository;

import com.android.cineflow.model.UserSubscription;
import com.android.cineflow.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
    Optional<UserSubscription> findFirstByUserIdAndStatusOrderByEndDateDesc(String userId, SubscriptionStatus status);
}
