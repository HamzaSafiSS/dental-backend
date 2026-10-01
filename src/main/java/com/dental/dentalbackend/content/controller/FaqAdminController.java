package com.dental.dentalbackend.content.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.content.dto.*;
import com.dental.dentalbackend.content.service.FaqService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "FAQ Management", description = "Admin manages FAQs and categories")
public class FaqAdminController {

    private final FaqService faqService;

    // ── FAQs ──────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/faqs")
    @Operation(summary = "List all FAQs (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<FaqResponse>>> listAllFaqs(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        Page<FaqResponse> result = faqService.listAllFaqs(categoryId, pageable);

        PagedResponse<FaqResponse> pagedResponse = PagedResponse.<FaqResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @GetMapping("/api/v1/faqs/{id}")
    @Operation(summary = "Get FAQ by ID")
    public ResponseEntity<ApiResponse<FaqResponse>> getFaqById(@PathVariable UUID id) {
        FaqResponse response = faqService.getFaqById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/v1/faqs")
    @Operation(summary = "Create a new FAQ")
    public ResponseEntity<ApiResponse<FaqResponse>> createFaq(
            @Valid @RequestBody CreateFaqRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        FaqResponse response = faqService.createFaq(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("FAQ created successfully", response));
    }

    @PutMapping("/api/v1/faqs/{id}")
    @Operation(summary = "Update an FAQ")
    public ResponseEntity<ApiResponse<FaqResponse>> updateFaq(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFaqRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        FaqResponse response = faqService.updateFaq(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("FAQ updated successfully", response));
    }

    @DeleteMapping("/api/v1/faqs/{id}")
    @Operation(summary = "Delete an FAQ")
    public ResponseEntity<ApiResponse<Void>> deleteFaq(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        faqService.deleteFaq(id, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("FAQ deleted successfully", null));
    }

    // ── Categories ────────────────────────────────────────────────────────

    @GetMapping("/api/v1/faq-categories")
    @Operation(summary = "List all FAQ categories")
    public ResponseEntity<ApiResponse<List<FaqCategoryResponse>>> listCategories() {
        List<FaqCategoryResponse> categories = faqService.listCategories(false);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping("/api/v1/faq-categories")
    @Operation(summary = "Create an FAQ category")
    public ResponseEntity<ApiResponse<FaqCategoryResponse>> createCategory(
            @Valid @RequestBody FaqCategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        FaqCategoryResponse response = faqService.createCategory(
                request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("FAQ category created successfully", response));
    }

    @PutMapping("/api/v1/faq-categories/{id}")
    @Operation(summary = "Update an FAQ category")
    public ResponseEntity<ApiResponse<FaqCategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody FaqCategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        FaqCategoryResponse response = faqService.updateCategory(
                id, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("FAQ category updated successfully", response));
    }
}
