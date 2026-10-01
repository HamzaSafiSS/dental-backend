package com.dental.dentalbackend.clinic.controller;

import com.dental.dentalbackend.clinic.dto.ClinicSettingsResponse;
import com.dental.dentalbackend.clinic.dto.UpdateClinicSettingsRequest;
import com.dental.dentalbackend.clinic.service.ClinicSettingsService;
import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Clinic Settings", description = "Clinic information and configuration")
public class ClinicController {

    private final ClinicSettingsService clinicSettingsService;

    @GetMapping("/api/v1/public/clinic")
    @Operation(summary = "Get public clinic information")
    public ResponseEntity<ApiResponse<ClinicSettingsResponse>> getPublicClinicInfo() {
        ClinicSettingsResponse response = clinicSettingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/clinic")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get full clinic settings (admin)")
    public ResponseEntity<ApiResponse<ClinicSettingsResponse>> getClinicSettings() {
        ClinicSettingsResponse response = clinicSettingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/api/v1/clinic")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update clinic settings")
    public ResponseEntity<ApiResponse<ClinicSettingsResponse>> updateClinicSettings(
            @Valid @RequestBody UpdateClinicSettingsRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        ClinicSettingsResponse response = clinicSettingsService.updateSettings(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Clinic settings updated successfully", response));
    }
}
