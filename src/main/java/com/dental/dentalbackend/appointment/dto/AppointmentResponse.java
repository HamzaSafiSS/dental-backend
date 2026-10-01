package com.dental.dentalbackend.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private UUID id;

    // Patient info
    private UUID patientId;
    private String patientName;

    // Doctor info
    private UUID doctorId;
    private String doctorName;
    private String doctorSpecialization;

    // Service info
    private UUID serviceId;
    private String serviceName;
    private int durationMinutes;
    private BigDecimal requiredPaymentAmount;

    // Schedule
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Status
    private String status;
    private String reasonForVisit;
    private String patientNotes;
    private String doctorNotes;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
