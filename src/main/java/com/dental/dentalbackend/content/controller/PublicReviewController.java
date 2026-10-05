package com.dental.dentalbackend.content.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.content.dto.GuestReviewRequest;
import com.dental.dentalbackend.content.dto.ReviewResponse;
import com.dental.dentalbackend.content.service.ReviewEmailService;
import com.dental.dentalbackend.content.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/reviews")
@RequiredArgsConstructor
@Tag(name = "Public Reviews", description = "Public-facing approved reviews and guest review submission")
public class PublicReviewController {

    private final ReviewService reviewService;
    private final ReviewEmailService reviewEmailService;

    @GetMapping
    @Operation(summary = "List approved patient reviews (paginated, optional featured filter)")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> listApprovedReviews(
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewResponse> result = reviewService.listPublicReviews(featured, pageable);

        PagedResponse<ReviewResponse> pagedResponse = PagedResponse.<ReviewResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @PostMapping
    @Operation(summary = "Submit a review as a guest visitor",
            description = "Sends an email notification to the clinic with the review details. No authentication required.")
    public ResponseEntity<ApiResponse<Void>> submitGuestReview(@Valid @RequestBody GuestReviewRequest request) {

        reviewEmailService.sendGuestReviewNotificationToClinic(request);

        return ResponseEntity.ok(
                ApiResponse.success("Thank you for your review! It has been submitted successfully.")
        );
    }
}

