package com.dental.dentalbackend.availability.controller;

import com.dental.dentalbackend.availability.dto.*;
import com.dental.dentalbackend.availability.service.AvailabilityService;
import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}")
@RequiredArgsConstructor
@Tag(name = "Doctor Availability", description = "Manage doctor weekly availability, time-off, and view open slots")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    // ── Availability ──────────────────────────────────────────────

    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Get doctor's weekly availability config")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getAvailability(
            @PathVariable UUID doctorId) {
        List<AvailabilityResponse> response = availabilityService.getDoctorAvailability(doctorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/availability")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set/replace doctor's weekly availability")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> setAvailability(
            @PathVariable UUID doctorId,
            @Valid @RequestBody List<AvailabilityRequest> requests,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        List<AvailabilityResponse> response = availabilityService.setDoctorAvailability(
                doctorId, requests, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Availability updated", response));
    }

    // ── Time-off ──────────────────────────────────────────────────

    @GetMapping("/time-off")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "View doctor's time-off periods")
    public ResponseEntity<ApiResponse<List<TimeOffResponse>>> getTimeOff(
            @PathVariable UUID doctorId) {
        List<TimeOffResponse> response = availabilityService.getDoctorTimeOff(doctorId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/time-off")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add time-off period for doctor")
    public ResponseEntity<ApiResponse<TimeOffResponse>> addTimeOff(
            @PathVariable UUID doctorId,
            @Valid @RequestBody TimeOffRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        TimeOffResponse response = availabilityService.addTimeOff(
                doctorId, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Time-off added", response));
    }

    // ── Available slots ───────────────────────────────────────────

    @GetMapping("/available-slots")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get available appointment slots for a doctor on a specific date")
    public ResponseEntity<ApiResponse<List<AvailableSlot>>> getAvailableSlots(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "30") int durationMinutes) {
        List<AvailableSlot> slots = availabilityService.getAvailableSlots(doctorId, date, durationMinutes);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }
}
