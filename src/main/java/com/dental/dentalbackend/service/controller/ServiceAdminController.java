package com.dental.dentalbackend.service.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.security.CustomUserDetails;
import com.dental.dentalbackend.service.dto.*;
import com.dental.dentalbackend.service.service.DentalServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.dental.dentalbackend.storage.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Service Management", description = "Admin manages service categories and dental services")
public class ServiceAdminController {

    private final DentalServiceService dentalServiceService;
    private final FileStorageService fileStorageService;

    // ── Categories ────────────────────────────────────────────────

    @GetMapping("/api/v1/service-categories")
    @Operation(summary = "List all service categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories() {
        List<CategoryResponse> categories = dentalServiceService.listAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping(value = "/api/v1/service-categories", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new service category (JSON)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        CategoryResponse response = dentalServiceService.createCategory(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    @PostMapping(value = "/api/v1/service-categories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new service category with icon upload (Multipart)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategoryMultipart(
            @ModelAttribute @Valid CreateCategoryRequest request,
            @RequestParam(value = "icon", required = false) MultipartFile icon,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "iconUrl", required = false) MultipartFile iconUrlFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        MultipartFile uploadFile = icon != null ? icon :
                (image != null ? image : (file != null ? file : iconUrlFile));
        if (uploadFile != null && !uploadFile.isEmpty()) {
            String storedUrl = fileStorageService.storeFile(uploadFile, "categories");
            request.setIconUrl(storedUrl);
        }
        CategoryResponse response = dentalServiceService.createCategory(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    @PutMapping(value = "/api/v1/service-categories/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a service category (JSON)")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        CategoryResponse response = dentalServiceService.updateCategory(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @PutMapping(value = "/api/v1/service-categories/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a service category with icon upload (Multipart)")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategoryMultipart(
            @PathVariable UUID id,
            @ModelAttribute @Valid UpdateCategoryRequest request,
            @RequestParam(value = "icon", required = false) MultipartFile icon,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "iconUrl", required = false) MultipartFile iconUrlFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        MultipartFile uploadFile = icon != null ? icon :
                (image != null ? image : (file != null ? file : iconUrlFile));
        if (uploadFile != null && !uploadFile.isEmpty()) {
            String storedUrl = fileStorageService.storeFile(uploadFile, "categories");
            request.setIconUrl(storedUrl);
        }
        CategoryResponse response = dentalServiceService.updateCategory(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    // ── Services ──────────────────────────────────────────────────

    @GetMapping("/api/v1/services")
    @Operation(summary = "List all services (including inactive)")
    public ResponseEntity<ApiResponse<PagedResponse<ServiceResponse>>> listAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceResponse> result = dentalServiceService.listAllServices(pageable);

        PagedResponse<ServiceResponse> pagedResponse = PagedResponse.<ServiceResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @PostMapping(value = "/api/v1/services", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new dental service (JSON)")
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @Valid @RequestBody CreateServiceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        ServiceResponse response = dentalServiceService.createService(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created successfully", response));
    }

    @PostMapping(value = "/api/v1/services", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new dental service with image upload (Multipart)")
    public ResponseEntity<ApiResponse<ServiceResponse>> createServiceMultipart(
            @ModelAttribute @Valid CreateServiceRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "imageUrl", required = false) MultipartFile imageUrlFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        MultipartFile uploadFile = image != null ? image : (file != null ? file : imageUrlFile);
        if (uploadFile != null && !uploadFile.isEmpty()) {
            String storedUrl = fileStorageService.storeFile(uploadFile, "services");
            request.setImageUrl(storedUrl);
        }
        ServiceResponse response = dentalServiceService.createService(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created successfully", response));
    }

    @PutMapping(value = "/api/v1/services/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a dental service (JSON)")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        ServiceResponse response = dentalServiceService.updateService(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", response));
    }

    @PutMapping(value = "/api/v1/services/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a dental service with image upload (Multipart)")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateServiceMultipart(
            @PathVariable UUID id,
            @ModelAttribute @Valid UpdateServiceRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "imageUrl", required = false) MultipartFile imageUrlFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        MultipartFile uploadFile = image != null ? image : (file != null ? file : imageUrlFile);
        if (uploadFile != null && !uploadFile.isEmpty()) {
            String storedUrl = fileStorageService.storeFile(uploadFile, "services");
            request.setImageUrl(storedUrl);
        }
        ServiceResponse response = dentalServiceService.updateService(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", response));
    }

    @PatchMapping("/api/v1/services/{id}")
    @Operation(summary = "Toggle service active/inactive")
    public ResponseEntity<ApiResponse<ServiceResponse>> patchService(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        ServiceResponse response = dentalServiceService.updateService(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", response));
    }
}
