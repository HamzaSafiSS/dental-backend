package com.dental.dentalbackend.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBeforeAfterCaseRequest {

    private UUID serviceId;

    private UUID patientId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @NotBlank(message = "Before image URL is required")
    @Size(max = 500, message = "Before image URL must not exceed 500 characters")
    private String beforeImageUrl;

    @NotBlank(message = "After image URL is required")
    @Size(max = 500, message = "After image URL must not exceed 500 characters")
    private String afterImageUrl;

    private Boolean hasPatientConsent;

    private Boolean active;

    private Integer displayOrder;
}
