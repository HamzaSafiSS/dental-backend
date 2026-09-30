package com.dental.dentalbackend.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private UUID id;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String secondaryPhone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String bloodType;
    private String profileImageUrl;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<EmergencyContactResponse> emergencyContacts;
    private List<MedicalHistoryResponse> medicalHistory;
    private List<AllergyResponse> allergies;
}
