package com.dental.dentalbackend.receptionist.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.receptionist.dto.CreateReceptionistRequest;
import com.dental.dentalbackend.receptionist.dto.ReceptionistResponse;
import com.dental.dentalbackend.receptionist.dto.UpdateReceptionistRequest;
import com.dental.dentalbackend.receptionist.service.ReceptionistService;
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

import com.dental.dentalbackend.storage.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receptionists")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Receptionist Management", description = "Admin-only receptionist CRUD")
public class ReceptionistController {

    private final ReceptionistService receptionistService;
    private final FileStorageService fileStorageService;

    @GetMapping
    @Operation(summary = "List all receptionists (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<ReceptionistResponse>>> listReceptionists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReceptionistResponse> result = receptionistService.listReceptionists(pageable);

        PagedResponse<ReceptionistResponse> pagedResponse = PagedResponse.<ReceptionistResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new receptionist account (JSON)")
    public ResponseEntity<ApiResponse<ReceptionistResponse>> createReceptionist(
            @Valid @RequestBody CreateReceptionistRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        ReceptionistResponse response = receptionistService.createReceptionist(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Receptionist created successfully", response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new receptionist account with image upload (Multipart)")
    public ResponseEntity<ApiResponse<ReceptionistResponse>> createReceptionistMultipart(
            @ModelAttribute @Valid CreateReceptionistRequest request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "profileImageUrl", required = false) MultipartFile profileImageUrlFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        MultipartFile uploadFile = profileImage != null ? profileImage :
                (image != null ? image : (file != null ? file : profileImageUrlFile));
        if (uploadFile != null && !uploadFile.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(uploadFile, "staff");
            request.setProfileImageUrl(imageUrl);
        }
        ReceptionistResponse response = receptionistService.createReceptionist(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Receptionist created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get receptionist by ID")
    public ResponseEntity<ApiResponse<ReceptionistResponse>> getReceptionistById(@PathVariable UUID id) {
        ReceptionistResponse response = receptionistService.getReceptionistById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update receptionist (JSON)")
    public ResponseEntity<ApiResponse<ReceptionistResponse>> updateReceptionist(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReceptionistRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        ReceptionistResponse response = receptionistService.updateReceptionist(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Receptionist updated successfully", response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update receptionist with image upload (Multipart)")
    public ResponseEntity<ApiResponse<ReceptionistResponse>> updateReceptionistMultipart(
            @PathVariable UUID id,
            @ModelAttribute @Valid UpdateReceptionistRequest request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "profileImageUrl", required = false) MultipartFile profileImageUrlFile,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        MultipartFile uploadFile = profileImage != null ? profileImage :
                (image != null ? image : (file != null ? file : profileImageUrlFile));
        if (uploadFile != null && !uploadFile.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(uploadFile, "staff");
            request.setProfileImageUrl(imageUrl);
        }
        ReceptionistResponse response = receptionistService.updateReceptionist(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Receptionist updated successfully", response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partial update receptionist (e.g. activate/deactivate)")
    public ResponseEntity<ApiResponse<ReceptionistResponse>> patchReceptionist(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReceptionistRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        ReceptionistResponse response = receptionistService.updateReceptionist(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Receptionist updated successfully", response));
    }
}
