package com.dental.dentalbackend.doctor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Admin creates a doctor account — provisions both User (DOCTOR role) and Doctor profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDoctorRequest {

    
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

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
}
