package com.dental.dentalbackend.clinical.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrescriptionRequest {

    private UUID appointmentId;
    private String diagnosis;
    private String notes;
    private List<PrescriptionItemRequest> items;
}
