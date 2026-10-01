package com.dental.dentalbackend.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeforeAfterCaseResponse {

    private UUID id;
    private UUID serviceId;
    private String serviceName;
    private UUID patientId;
    private String title;
    private String description;
    private String beforeImageUrl;
    private String afterImageUrl;
    private boolean hasPatientConsent;
    private boolean active;
    private int displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
