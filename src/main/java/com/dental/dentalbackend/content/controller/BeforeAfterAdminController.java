package com.dental.dentalbackend.content.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.content.dto.BeforeAfterCaseResponse;
import com.dental.dentalbackend.content.dto.CreateBeforeAfterCaseRequest;
import com.dental.dentalbackend.content.dto.UpdateBeforeAfterCaseRequest;
import com.dental.dentalbackend.content.service.BeforeAfterService;
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
@RequestMapping("/api/v1/before-after")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Before/After Management", description = "Admin manages before/after treatment cases")
public class BeforeAfterAdminController {

    private final BeforeAfterService beforeAfterService;

    @GetMapping
    @Operation(summary = "List all before/after cases (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<BeforeAfterCaseResponse>>> listAllCases(
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        Page<BeforeAfterCaseResponse> result = beforeAfterService.listAllCases(serviceId, pageable);

        PagedResponse<BeforeAfterCaseResponse> pagedResponse = PagedResponse.<BeforeAfterCaseResponse>builder()
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
    @Operation(summary = "Get before/after case by ID")
    public ResponseEntity<ApiResponse<BeforeAfterCaseResponse>> getCaseById(@PathVariable UUID id) {
        BeforeAfterCaseResponse response = beforeAfterService.getCaseById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new before/after case (consent required if active)")
    public ResponseEntity<ApiResponse<BeforeAfterCaseResponse>> createCase(
            @Valid @RequestBody CreateBeforeAfterCaseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        BeforeAfterCaseResponse response = beforeAfterService.createCase(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Before/after case created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a before/after case (consent required if active)")
    public ResponseEntity<ApiResponse<BeforeAfterCaseResponse>> updateCase(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBeforeAfterCaseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        BeforeAfterCaseResponse response = beforeAfterService.updateCase(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Before/after case updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a before/after case")
    public ResponseEntity<ApiResponse<Void>> deleteCase(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        beforeAfterService.deleteCase(id, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Before/after case deleted successfully", null));
    }
}
