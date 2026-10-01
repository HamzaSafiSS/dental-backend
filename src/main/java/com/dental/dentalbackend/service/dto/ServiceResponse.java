package com.dental.dentalbackend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String slug;
    private String shortDescription;
    private String fullDescription;
    private int durationMinutes;
    private BigDecimal requiredPaymentAmount;
    private String imageUrl;
    private boolean active;
    private LocalDateTime createdAt;
}
