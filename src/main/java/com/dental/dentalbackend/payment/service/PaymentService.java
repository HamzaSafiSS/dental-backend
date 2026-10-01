package com.dental.dentalbackend.payment.service;

import com.dental.dentalbackend.appointment.entity.Appointment;
import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import com.dental.dentalbackend.appointment.service.AppointmentService;
import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.BusinessRuleException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.payment.dto.PaymentResponse;
import com.dental.dentalbackend.payment.dto.SubmitPaymentRequest;
import com.dental.dentalbackend.payment.dto.VerifyPaymentRequest;
import com.dental.dentalbackend.payment.entity.AppointmentPayment;
import com.dental.dentalbackend.payment.entity.PaymentStatus;
import com.dental.dentalbackend.payment.repository.AppointmentPaymentRepository;
import com.dental.dentalbackend.user.entity.User;
import com.dental.dentalbackend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AppointmentPaymentRepository paymentRepository;
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // ── Patient submits payment proof ─────────────────────────────

    @Transactional
    public PaymentResponse submitPayment(UUID appointmentId, SubmitPaymentRequest request,
                                           UUID performedBy, HttpServletRequest httpRequest) {
        Appointment appointment = appointmentService.findAppointmentOrThrow(appointmentId);

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Payment can only be submitted for appointments in PENDING_PAYMENT status");
        }

        // Create or update payment record
        AppointmentPayment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> {
                    AppointmentPayment p = new AppointmentPayment();
                    p.setAppointment(appointment);
                    p.setRequiredAmount(appointment.getService().getRequiredPaymentAmount());
                    return p;
                });

        payment.setPaymentReference(request.getPaymentReference());
        payment.setPaymentScreenshotUrl(request.getPaymentScreenshotUrl());
        payment.setPaidAt(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        auditService.log(performedBy, "PAYMENT_SUBMITTED", "AppointmentPayment", payment.getId(),
                "Payment proof submitted for appointment " + appointmentId, httpRequest);

        log.info("Payment submitted for appointment: {}", appointmentId);
        return mapToResponse(payment);
    }

    // ── Admin/Receptionist verifies or rejects payment ────────────

    @Transactional
    public PaymentResponse verifyPayment(UUID appointmentId, VerifyPaymentRequest request,
                                           UUID performedBy, HttpServletRequest httpRequest) {
        AppointmentPayment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "appointmentId", appointmentId));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING payments can be verified or rejected");
        }

        User verifier = userRepository.findById(performedBy)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", performedBy));

        Appointment appointment = payment.getAppointment();

        if (request.getApproved()) {
            // Approve payment → transition appointment to PAYMENT_VERIFIED
            payment.setPaymentStatus(PaymentStatus.VERIFIED);
            payment.setVerifiedAt(LocalDateTime.now());
            payment.setVerifiedBy(verifier);
            payment.setVerificationNote(request.getVerificationNote());
            paymentRepository.save(payment);

            appointmentService.transitionStatus(
                    appointment, AppointmentStatus.PAYMENT_VERIFIED, performedBy, "Payment verified");

            auditService.log(performedBy, "PAYMENT_VERIFIED", "AppointmentPayment", payment.getId(),
                    "Payment verified for appointment " + appointmentId, httpRequest);

            log.info("Payment verified for appointment: {}", appointmentId);
        } else {
            // Reject payment → stays in PENDING_PAYMENT (patient can resubmit)
            payment.setPaymentStatus(PaymentStatus.REJECTED);
            payment.setVerifiedAt(LocalDateTime.now());
            payment.setVerifiedBy(verifier);
            payment.setVerificationNote(request.getVerificationNote());
            paymentRepository.save(payment);

            auditService.log(performedBy, "PAYMENT_REJECTED", "AppointmentPayment", payment.getId(),
                    "Payment rejected for appointment " + appointmentId
                            + ". Reason: " + request.getVerificationNote(), httpRequest);

            log.info("Payment rejected for appointment: {}", appointmentId);
        }

        return mapToResponse(payment);
    }

    // ── Get payment by appointment ────────────────────────────────

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByAppointmentId(UUID appointmentId) {
        AppointmentPayment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "appointmentId", appointmentId));
        return mapToResponse(payment);
    }

    // ── List pending payments (verification queue) ────────────────

    @Transactional(readOnly = true)
    public Page<PaymentResponse> listPendingPayments(Pageable pageable) {
        return paymentRepository.findAllByPaymentStatus(PaymentStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }

    // ── Mapper ────────────────────────────────────────────────────

    private PaymentResponse mapToResponse(AppointmentPayment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .appointmentId(payment.getAppointment().getId())
                .requiredAmount(payment.getRequiredAmount())
                .paymentStatus(payment.getPaymentStatus().name())
                .paymentReference(payment.getPaymentReference())
                .paymentScreenshotUrl(payment.getPaymentScreenshotUrl())
                .paidAt(payment.getPaidAt())
                .verifiedAt(payment.getVerifiedAt())
                .verifiedById(payment.getVerifiedBy() != null ? payment.getVerifiedBy().getId() : null)
                .verifiedByName(payment.getVerifiedBy() != null
                        ? payment.getVerifiedBy().getFirstName() + " " + payment.getVerifiedBy().getLastName()
                        : null)
                .verificationNote(payment.getVerificationNote())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
