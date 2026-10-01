package com.dental.dentalbackend.clinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicSettingsResponse {

    private UUID id;
    private String clinicName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    private String email;
    private String emergencyContact;
    private String whatsapp;
    private String openingHours;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String logoUrl;
    private String websiteUrl;
    private String about;
}
