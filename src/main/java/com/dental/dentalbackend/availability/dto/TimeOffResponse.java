package com.dental.dentalbackend.availability.dto;

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
public class TimeOffResponse {

    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private boolean approved;
}
