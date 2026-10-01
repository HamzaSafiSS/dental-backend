package com.dental.dentalbackend.clinical.controller;

import com.dental.dentalbackend.clinical.dto.*;
import com.dental.dentalbackend.clinical.service.ClinicalService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients/{patientId}")
@RequiredArgsConstructor
@Tag(name = "Clinical Records", description = "Doctor creates clinical records; patients view their own")
public class ClinicalController {

    private final ClinicalService clinicalService;

    // ── Dental Records ────────────────────────────────────────────

    @PostMapping("/dental-records")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Create dental record for patient")
    public ResponseEntity<ApiResponse<DentalRecordResponse>> createDentalRecord(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreateDentalRecordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        DentalRecordResponse response = clinicalService.createDentalRecord(
                patientId, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dental record created", response));
    }

    @GetMapping("/dental-records")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    @Operation(summary = "View dental records for patient")
    public ResponseEntity<ApiResponse<PagedResponse<DentalRecordResponse>>> getDentalRecords(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DentalRecordResponse> result = clinicalService.getPatientDentalRecords(patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(result)));
    }

    // ── Treatment Plans ───────────────────────────────────────────

    @PostMapping("/treatment-plans")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Create treatment plan for patient")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> createTreatmentPlan(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreateTreatmentPlanRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        TreatmentPlanResponse response = clinicalService.createTreatmentPlan(
                patientId, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Treatment plan created", response));
    }

    @GetMapping("/treatment-plans")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    @Operation(summary = "View treatment plans for patient")
    public ResponseEntity<ApiResponse<PagedResponse<TreatmentPlanResponse>>> getTreatmentPlans(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TreatmentPlanResponse> result = clinicalService.getPatientTreatmentPlans(patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(result)));
    }

    @GetMapping("/treatment-plans/{planId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    @Operation(summary = "Get treatment plan details")
    public ResponseEntity<ApiResponse<TreatmentPlanResponse>> getTreatmentPlan(
            @PathVariable UUID patientId,
            @PathVariable UUID planId) {
        TreatmentPlanResponse response = clinicalService.getTreatmentPlanById(planId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── Prescriptions ─────────────────────────────────────────────

    @PostMapping("/prescriptions")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Create prescription for patient")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> createPrescription(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreatePrescriptionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        PrescriptionResponse response = clinicalService.createPrescription(
                patientId, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Prescription created", response));
    }

    @GetMapping("/prescriptions")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    @Operation(summary = "View prescriptions for patient")
    public ResponseEntity<ApiResponse<PagedResponse<PrescriptionResponse>>> getPrescriptions(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PrescriptionResponse> result = clinicalService.getPatientPrescriptions(patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(result)));
    }

    @GetMapping("/prescriptions/{prescriptionId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    @Operation(summary = "Get prescription details")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescription(
            @PathVariable UUID patientId,
            @PathVariable UUID prescriptionId) {
        PrescriptionResponse response = clinicalService.getPrescriptionById(prescriptionId);
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
