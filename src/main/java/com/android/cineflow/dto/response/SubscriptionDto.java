package com.android.cineflow.dto.response;

import com.android.cineflow.model.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionDto {
    private Integer id;
    private String packageName;
    private BigDecimal price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SubscriptionStatus status;
}
