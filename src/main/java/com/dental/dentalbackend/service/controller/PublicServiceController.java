package com.dental.dentalbackend.service.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.service.dto.CategoryResponse;
import com.dental.dentalbackend.service.dto.ServiceResponse;
import com.dental.dentalbackend.service.service.DentalServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/services")
@RequiredArgsConstructor
@Tag(name = "Public Services", description = "Public-facing dental service catalog")
public class PublicServiceController {

    private final DentalServiceService dentalServiceService;

    @GetMapping
    @Operation(summary = "List active services for the public website")
    public ResponseEntity<ApiResponse<PagedResponse<ServiceResponse>>> listActiveServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceResponse> result = dentalServiceService.listActiveServices(pageable);

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

    @GetMapping("/{slug}")
    @Operation(summary = "Get service details by slug")
    public ResponseEntity<ApiResponse<ServiceResponse>> getServiceBySlug(@PathVariable String slug) {
        ServiceResponse response = dentalServiceService.getServiceBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/categories")
    @Operation(summary = "List active service categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listActiveCategories() {
        List<CategoryResponse> categories = dentalServiceService.listActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
