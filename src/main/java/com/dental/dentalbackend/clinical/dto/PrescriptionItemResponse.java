package com.dental.dentalbackend.clinical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItemResponse {

    private UUID id;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private Integer quantity;
    private String instructions;
}
