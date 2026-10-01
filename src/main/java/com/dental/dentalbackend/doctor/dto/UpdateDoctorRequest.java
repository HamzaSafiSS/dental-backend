package com.dental.dentalbackend.doctor.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Admin updates doctor profile and/or user fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDoctorRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    @Size(max = 200)
    private String specialization;

    @Size(max = 500)
    private String qualification;

    private Integer experienceYears;

    private String bio;

    @Size(max = 100)
    private String licenseNumber;

    private String profileImageUrl;

    private BigDecimal consultationFee;

    private Boolean active;
}
