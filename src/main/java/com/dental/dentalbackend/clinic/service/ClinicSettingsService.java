package com.dental.dentalbackend.clinic.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.clinic.dto.ClinicSettingsResponse;
import com.dental.dentalbackend.clinic.dto.UpdateClinicSettingsRequest;
import com.dental.dentalbackend.clinic.entity.ClinicSettings;
import com.dental.dentalbackend.clinic.repository.ClinicSettingsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicSettingsService {

    private final ClinicSettingsRepository clinicSettingsRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public ClinicSettingsResponse getSettings() {
        return mapToResponse(clinicSettingsRepository.getSettings());
    }

    @Transactional
    public ClinicSettingsResponse updateSettings(UpdateClinicSettingsRequest request,
                                                  UUID performedBy, HttpServletRequest httpRequest) {
        ClinicSettings settings = clinicSettingsRepository.getSettings();

        if (request.getClinicName() != null) settings.setClinicName(request.getClinicName());
        if (request.getAddress() != null) settings.setAddress(request.getAddress());
        if (request.getCity() != null) settings.setCity(request.getCity());
        if (request.getState() != null) settings.setState(request.getState());
        if (request.getZipCode() != null) settings.setZipCode(request.getZipCode());
        if (request.getPhone() != null) settings.setPhone(request.getPhone());
        if (request.getEmail() != null) settings.setEmail(request.getEmail());
        if (request.getEmergencyContact() != null) settings.setEmergencyContact(request.getEmergencyContact());
        if (request.getWhatsapp() != null) settings.setWhatsapp(request.getWhatsapp());
        if (request.getOpeningHours() != null) settings.setOpeningHours(request.getOpeningHours());
        if (request.getLatitude() != null) settings.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) settings.setLongitude(request.getLongitude());
        if (request.getLogoUrl() != null) settings.setLogoUrl(request.getLogoUrl());
        if (request.getWebsiteUrl() != null) settings.setWebsiteUrl(request.getWebsiteUrl());
        if (request.getAbout() != null) settings.setAbout(request.getAbout());

        settings = clinicSettingsRepository.save(settings);

        auditService.log(performedBy, "CLINIC_SETTINGS_UPDATED", "ClinicSettings", settings.getId(),
                "Clinic settings updated", httpRequest);

        return mapToResponse(settings);
    }

    private ClinicSettingsResponse mapToResponse(ClinicSettings settings) {
        return ClinicSettingsResponse.builder()
                .id(settings.getId())
                .clinicName(settings.getClinicName())
                .address(settings.getAddress())
                .city(settings.getCity())
                .state(settings.getState())
                .zipCode(settings.getZipCode())
                .phone(settings.getPhone())
                .email(settings.getEmail())
                .emergencyContact(settings.getEmergencyContact())
                .whatsapp(settings.getWhatsapp())
                .openingHours(settings.getOpeningHours())
                .latitude(settings.getLatitude())
                .longitude(settings.getLongitude())
                .logoUrl(settings.getLogoUrl())
                .websiteUrl(settings.getWebsiteUrl())
                .about(settings.getAbout())
                .build();
    }
}
