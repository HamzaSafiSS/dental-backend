package com.dental.dentalbackend.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {

    private UUID categoryId;

    @NotBlank(message = "Service name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String slug;

    @Size(max = 500)
    private String shortDescription;

    private String fullDescription;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private int durationMinutes = 30;

    @Min(value = 0, message = "Payment amount cannot be negative")
    private BigDecimal requiredPaymentAmount = BigDecimal.ZERO;

    @Size(max = 500)
    private String imageUrl;
}
