package com.dental.dentalbackend.clinical.dto;

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
public class DentalRecordResponse {

    private UUID id;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private UUID appointmentId;
    private String recordType;
    private String chiefComplaint;
    private String clinicalFindings;
    private String diagnosis;
    private String treatmentPerformed;
    private String notes;
    private String toothNumber;
    private LocalDateTime createdAt;
}
