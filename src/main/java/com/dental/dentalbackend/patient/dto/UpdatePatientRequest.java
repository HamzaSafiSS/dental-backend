package com.dental.dentalbackend.patient.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Used by ADMIN/RECEPTIONIST to update patient profile fields.
 * Patient self-update is NOT allowed per business requirement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePatientRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    private LocalDate dateOfBirth;
    private String gender;

    @Size(max = 20)
    private String secondaryPhone;

    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String zipCode;

    @Size(max = 5)
    private String bloodType;

    private String notes;
    private Boolean active;
}
