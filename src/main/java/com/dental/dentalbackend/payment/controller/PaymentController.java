package com.dental.dentalbackend.payment.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.common.dto.PagedResponse;
import com.dental.dentalbackend.payment.dto.PaymentResponse;
import com.dental.dentalbackend.payment.dto.SubmitPaymentRequest;
import com.dental.dentalbackend.payment.dto.VerifyPaymentRequest;
import com.dental.dentalbackend.payment.service.PaymentService;
import com.dental.dentalbackend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment submission and verification")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/appointments/{appointmentId}/submit")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Submit payment proof for an appointment")
    public ResponseEntity<ApiResponse<PaymentResponse>> submitPayment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody SubmitPaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        PaymentResponse response = paymentService.submitPayment(
                appointmentId, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment proof submitted", response));
    }

    @PostMapping("/appointments/{appointmentId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "Verify or reject a submitted payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {
        PaymentResponse response = paymentService.verifyPayment(
                appointmentId, request, userDetails.getUser().getId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                request.getApproved() ? "Payment verified" : "Payment rejected", response));
    }

    @GetMapping("/appointments/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment details for an appointment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID appointmentId) {
        PaymentResponse response = paymentService.getPaymentByAppointmentId(appointmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @Operation(summary = "List pending payments (verification queue)")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> listPendingPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> result = paymentService.listPendingPayments(pageable);

        PagedResponse<PaymentResponse> pagedResponse = PagedResponse.<PaymentResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }
}
