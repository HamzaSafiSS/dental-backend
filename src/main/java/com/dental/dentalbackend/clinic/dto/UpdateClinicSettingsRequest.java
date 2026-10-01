package com.dental.dentalbackend.clinic.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClinicSettingsRequest {

    @Size(max = 255)
    private String clinicName;

    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String zipCode;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String emergencyContact;

    @Size(max = 20)
    private String whatsapp;

    private String openingHours;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 500)
    private String websiteUrl;

    private String about;
}
