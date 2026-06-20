package com.android.cineflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserDto {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private Instant createdAt;
    private String subscriptionPlan;
    private LocalDateTime subscriptionEndDate;
}
