package com.dental.dentalbackend.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryResponse {

    private UUID id;
    private String condition;
    private String description;
    private LocalDate diagnosedDate;
    private boolean current;
}
