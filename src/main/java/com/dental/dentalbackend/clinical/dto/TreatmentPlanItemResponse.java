package com.dental.dentalbackend.clinical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPlanItemResponse {

    private UUID id;
    private UUID serviceId;
    private String serviceName;
    private String toothNumber;
    private String description;
    private int sequenceOrder;
    private BigDecimal estimatedCost;
    private String status;
    private String notes;
}
