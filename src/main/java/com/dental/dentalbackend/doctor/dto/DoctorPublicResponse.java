package com.dental.dentalbackend.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Limited doctor info exposed via public endpoints — no email, login, or admin fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPublicResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String qualification;
    private String bio;
    private String profileImageUrl;
}
