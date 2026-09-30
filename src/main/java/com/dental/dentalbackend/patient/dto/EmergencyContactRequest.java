package com.dental.dentalbackend.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactRequest {

    @NotBlank(message = "Contact name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 50)
    private String relationship;

    @NotBlank(message = "Contact phone is required")
    @Size(max = 20)
    private String phone;

    private String email;
}
