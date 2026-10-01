package com.dental.dentalbackend.payment.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Patient submits payment proof (screenshot URL and/or reference number).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitPaymentRequest {

    @Size(max = 255)
    private String paymentReference;

    @Size(max = 500)
    private String paymentScreenshotUrl;
}
