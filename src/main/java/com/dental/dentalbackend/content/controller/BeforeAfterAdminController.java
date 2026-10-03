package com.dental.dentalbackend.content.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.content.dto.BeforeAfterCaseResponse;
import com.dental.dentalbackend.content.dto.CreateBeforeAfterCaseRequest;
import com.dental.dentalbackend.content.dto.UpdateBeforeAfterCaseRequest;
import com.dental.dentalbackend.content.service.BeforeAfterService;
import com.dental.dentalbackend.security.CustomUserDetails;
import com.dental.dentalbackend.storage.service.FileStorageService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/before-after")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Before/After Management", description = "Admin manages before/after treatment cases")
public class BeforeAfterAdminController {

    private final BeforeAfterService beforeAfterService;
    private final FileStorageService fileStorageService;

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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new before/after case (JSON)")
    public ResponseEntity<ApiResponse<BeforeAfterCaseResponse>> createCase(
            @Valid @RequestBody CreateBeforeAfterCaseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        BeforeAfterCaseResponse response = beforeAfterService.createCase(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Before/after case created successfully", response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new before/after case with image upload (Multipart)")
    public ResponseEntity<ApiResponse<BeforeAfterCaseResponse>> createCaseMultipart(
            @ModelAttribute @Valid CreateBeforeAfterCaseRequest request,
            @RequestParam(value = "beforeImage", required = false) MultipartFile beforeImage,
            @RequestParam(value = "afterImage", required = false) MultipartFile afterImage,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        if (beforeImage != null && !beforeImage.isEmpty()) {
            String storedUrl = fileStorageService.storeFile(beforeImage, "before-after");
            request.setBeforeImageUrl(storedUrl);
        }
        if (afterImage != null && !afterImage.isEmpty()) {
            String storedUrl = fileStorageService.storeFile(afterImage, "before-after");
            request.setAfterImageUrl(storedUrl);
        }
        BeforeAfterCaseResponse response = beforeAfterService.createCase(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Before/after case created successfully", response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a before/after case (JSON)")
    public ResponseEntity<ApiResponse<BeforeAfterCaseResponse>> updateCase(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBeforeAfterCaseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        BeforeAfterCaseResponse response = beforeAfterService.updateCase(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Before/after case updated successfully", response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a before/after case with image upload (Multipart)")
    public ResponseEntity<ApiResponse<BeforeAfterCaseResponse>> updateCaseMultipart(
            @PathVariable UUID id,
            @ModelAttribute @Valid UpdateBeforeAfterCaseRequest request,
            @RequestParam(value = "beforeImage", required = false) MultipartFile beforeImage,
            @RequestParam(value = "afterImage", required = false) MultipartFile afterImage,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        if (beforeImage != null && !beforeImage.isEmpty()) {
            String storedUrl = fileStorageService.storeFile(beforeImage, "before-after");
            request.setBeforeImageUrl(storedUrl);
        }
        if (afterImage != null && !afterImage.isEmpty()) {
            String storedUrl = fileStorageService.storeFile(afterImage, "before-after");
            request.setAfterImageUrl(storedUrl);
        }
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

