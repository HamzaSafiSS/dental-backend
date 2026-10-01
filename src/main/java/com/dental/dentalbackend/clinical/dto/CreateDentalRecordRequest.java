package com.dental.dentalbackend.clinical.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDentalRecordRequest {

    private UUID appointmentId;

    @Size(max = 50)
    private String recordType;

    private String chiefComplaint;
    private String clinicalFindings;
    private String diagnosis;
    private String treatmentPerformed;
    private String notes;

    @Size(max = 10)
    private String toothNumber;
}
