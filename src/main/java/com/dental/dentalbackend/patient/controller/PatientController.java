package com.dental.dentalbackend.patient.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.patient.dto.CreatePatientRequest;
import com.dental.dentalbackend.patient.dto.PatientResponse;
import com.dental.dentalbackend.patient.dto.UpdatePatientRequest;
import com.dental.dentalbackend.patient.service.PatientService;
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
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patient Management", description = "Patient CRUD and self-service profile")
public class PatientController {

    private final PatientService patientService;

    // ── Patient self-service ──────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get own patient profile")
    public ResponseEntity<ApiResponse<PatientResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PatientResponse response = patientService.getMyProfile(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // NOTE: Patient self-update is intentionally NOT exposed per business requirement.

    // ── Admin/Receptionist management ─────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "List all patients (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<PatientResponse>>> listPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PatientResponse> result = patientService.listPatients(pageable);

        PagedResponse<PatientResponse> pagedResponse = PagedResponse.<PatientResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Get patient details by ID")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable UUID id) {
        PatientResponse response = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Register a new patient (creates user + patient profile)")
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(
            @Valid @RequestBody CreatePatientRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        PatientResponse response = patientService.createPatient(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Update patient profile")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        PatientResponse response = patientService.updatePatient(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", response));
    }
}
