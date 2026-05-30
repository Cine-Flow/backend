package com.android.cineflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDto {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String role;
    private long favoriteCount;
    private long watchHistoryCount;
    private SubscriptionDto currentSubscription;
}
