package com.dental.dentalbackend.appointment.controller;

import com.dental.dentalbackend.appointment.dto.AppointmentResponse;
import com.dental.dentalbackend.appointment.dto.CreateAppointmentRequest;
import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import com.dental.dentalbackend.appointment.service.AppointmentService;
import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment scheduling and management")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ── Patient creates own appointment ───────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'RECEPTIONIST')")
    @Operation(summary = "Create a new appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        AppointmentResponse response = appointmentService.createAppointment(
                request, userDetails.getUser().getId(), userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment created successfully", response));
    }

    // ── Receptionist creates appointment for a patient ────────────

    @PostMapping("/for-patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Create appointment for a specific patient (staff)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointmentForPatient(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        AppointmentResponse response = appointmentService.createAppointmentForPatient(
                request, patientId, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment created successfully", response));
    }

    // ── Patient views own appointments ────────────────────────────

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get my appointments (patient)")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentResponse>>> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponse> result = appointmentService.getMyAppointments(
                userDetails.getUser().getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(result)));
    }

    // ── Doctor views own appointments ─────────────────────────────

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get my appointments (doctor)")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentResponse>>> getDoctorAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponse> result = appointmentService.getDoctorAppointments(
                userDetails.getUser().getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(result)));
    }

    // ── Admin/Receptionist: list all appointments ─────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "List all appointments (paginated, filterable)")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentResponse>>> listAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponse> result = appointmentService.listAppointments(
                status, doctorId, patientId, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(result)));
    }

    // ── Get appointment by ID ─────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get appointment details by ID")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable UUID id) {
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── Helper ────────────────────────────────────────────────────

    private <T> PagedResponse<T> buildPagedResponse(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
