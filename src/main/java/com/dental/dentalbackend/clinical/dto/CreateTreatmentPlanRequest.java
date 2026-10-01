package com.dental.dentalbackend.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTreatmentPlanRequest {

    private UUID appointmentId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private LocalDate startDate;
    private LocalDate estimatedEndDate;
    private BigDecimal totalEstimatedCost;
    private String notes;
    private List<TreatmentPlanItemRequest> items;
}
