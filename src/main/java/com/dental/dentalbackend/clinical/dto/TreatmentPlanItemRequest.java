package com.dental.dentalbackend.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPlanItemRequest {

    private UUID serviceId;
    private String toothNumber;

    @NotBlank(message = "Description is required")
    private String description;

    private int sequenceOrder;
    private BigDecimal estimatedCost;
    private String notes;
}
