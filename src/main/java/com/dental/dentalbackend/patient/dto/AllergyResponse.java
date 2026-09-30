package com.dental.dentalbackend.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergyResponse {

    private UUID id;
    private String allergen;
    private String severity;
    private String reaction;
}
