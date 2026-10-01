package com.dental.dentalbackend.appointment.controller;

import com.dental.dentalbackend.appointment.dto.AppointmentResponse;
import com.dental.dentalbackend.appointment.dto.CancelAppointmentRequest;
import com.dental.dentalbackend.appointment.dto.DoctorNotesRequest;
import com.dental.dentalbackend.appointment.service.AppointmentWorkflowService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments/{appointmentId}")
@RequiredArgsConstructor
@Tag(name = "Appointment Workflow", description = "Appointment lifecycle state transitions")
public class AppointmentWorkflowController {

    private final AppointmentWorkflowService workflowService;

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Confirm appointment (after payment verified)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirm(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        AppointmentResponse response = workflowService.confirmAppointment(
                appointmentId, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Appointment confirmed", response));
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Check-in patient for their appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> checkIn(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        AppointmentResponse response = workflowService.checkInAppointment(
                appointmentId, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Patient checked in", response));
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Start treatment (in progress)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> start(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        AppointmentResponse response = workflowService.startAppointment(
                appointmentId, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Treatment started", response));
    }

    @PostMapping("/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Complete treatment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> complete(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        AppointmentResponse response = workflowService.completeAppointment(
                appointmentId, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Treatment completed", response));
    }

    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody CancelAppointmentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        AppointmentResponse response = workflowService.cancelAppointment(
                appointmentId, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled", response));
    }

    @PostMapping("/no-show")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Mark patient as no-show")
    public ResponseEntity<ApiResponse<AppointmentResponse>> noShow(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        AppointmentResponse response = workflowService.markNoShow(
                appointmentId, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Patient marked as no-show", response));
    }

    @PostMapping("/doctor-notes")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Add doctor notes to appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> addDoctorNotes(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody DoctorNotesRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        AppointmentResponse response = workflowService.addDoctorNotes(
                appointmentId, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Doctor notes updated", response));
    }
}
