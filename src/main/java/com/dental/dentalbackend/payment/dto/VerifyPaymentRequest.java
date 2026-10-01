package com.dental.dentalbackend.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin/Receptionist verifies or rejects a payment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {

    @NotNull(message = "Approved flag is required")
    private Boolean approved;

    private String verificationNote;
}
