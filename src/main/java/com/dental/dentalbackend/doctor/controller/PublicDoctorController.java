package com.dental.dentalbackend.doctor.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.doctor.dto.DoctorPublicResponse;
import com.dental.dentalbackend.doctor.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/doctors")
@RequiredArgsConstructor
@Tag(name = "Public Doctors", description = "Public-facing doctor listing for the website")
public class PublicDoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "List active doctors for the public website")
    public ResponseEntity<ApiResponse<PagedResponse<DoctorPublicResponse>>> listPublicDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DoctorPublicResponse> result = doctorService.listPublicDoctors(pageable);

        PagedResponse<DoctorPublicResponse> pagedResponse = PagedResponse.<DoctorPublicResponse>builder()
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
    @Operation(summary = "Get public doctor profile by ID")
    public ResponseEntity<ApiResponse<DoctorPublicResponse>> getPublicDoctorById(@PathVariable UUID id) {
        DoctorPublicResponse response = doctorService.getPublicDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
