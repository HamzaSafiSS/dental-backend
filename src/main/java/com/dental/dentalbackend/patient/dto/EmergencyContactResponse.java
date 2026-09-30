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
public class EmergencyContactResponse {

    private UUID id;
    private String name;
    private String relationship;
    private String phone;
    private String email;
}
