package com.dental.dentalbackend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;
    private UUID appointmentId;
    private BigDecimal requiredAmount;
    private String paymentStatus;
    private String paymentReference;
    private String paymentScreenshotUrl;
    private LocalDateTime paidAt;
    private LocalDateTime verifiedAt;
    private UUID verifiedById;
    private String verifiedByName;
    private String verificationNote;
    private LocalDateTime createdAt;
}
