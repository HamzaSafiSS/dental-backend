package com.dental.dentalbackend.appointment.service;

import com.dental.dentalbackend.appointment.dto.AppointmentResponse;
import com.dental.dentalbackend.appointment.dto.CancelAppointmentRequest;
import com.dental.dentalbackend.appointment.dto.DoctorNotesRequest;
import com.dental.dentalbackend.appointment.entity.Appointment;
import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.user.entity.User;
import com.dental.dentalbackend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles appointment lifecycle transitions:
 *   PAYMENT_VERIFIED → CONFIRMED → CHECKED_IN → IN_PROGRESS → COMPLETED
 *   Any cancellable state → CANCELLED
 *   CONFIRMED → NO_SHOW
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentWorkflowService {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // ── Confirm (after payment verified) ──────────────────────────

    @Transactional
    public AppointmentResponse confirmAppointment(UUID appointmentId, UUID performedBy, HttpServletRequest httpRequest) {
        Appointment appointment = appointmentService.findAppointmentOrThrow(appointmentId);
        appointmentService.transitionStatus(appointment, AppointmentStatus.CONFIRMED, performedBy, "Appointment confirmed");

        auditService.log(performedBy, "APPOINTMENT_CONFIRMED", "Appointment", appointmentId,
                "Appointment confirmed", httpRequest);

        return appointmentService.mapToResponse(appointment);
    }

    // ── Check-in ──────────────────────────────────────────────────

    @Transactional
    public AppointmentResponse checkInAppointment(UUID appointmentId, UUID performedBy, HttpServletRequest httpRequest) {
        Appointment appointment = appointmentService.findAppointmentOrThrow(appointmentId);
        appointmentService.transitionStatus(appointment, AppointmentStatus.CHECKED_IN, performedBy, "Patient checked in");

        auditService.log(performedBy, "APPOINTMENT_CHECKED_IN", "Appointment", appointmentId,
                "Patient checked in", httpRequest);

        return appointmentService.mapToResponse(appointment);
    }

    // ── Start (in progress) ───────────────────────────────────────

    @Transactional
    public AppointmentResponse startAppointment(UUID appointmentId, UUID performedBy, HttpServletRequest httpRequest) {
        Appointment appointment = appointmentService.findAppointmentOrThrow(appointmentId);
        appointmentService.transitionStatus(appointment, AppointmentStatus.IN_PROGRESS, performedBy, "Treatment started");

        auditService.log(performedBy, "APPOINTMENT_STARTED", "Appointment", appointmentId,
                "Treatment started", httpRequest);

        return appointmentService.mapToResponse(appointment);
    }

    // ── Complete ──────────────────────────────────────────────────

    @Transactional
    public AppointmentResponse completeAppointment(UUID appointmentId, UUID performedBy, HttpServletRequest httpRequest) {
        Appointment appointment = appointmentService.findAppointmentOrThrow(appointmentId);
        appointmentService.transitionStatus(appointment, AppointmentStatus.COMPLETED, performedBy, "Treatment completed");

        auditService.log(performedBy, "APPOINTMENT_COMPLETED", "Appointment", appointmentId,
                "Treatment completed", httpRequest);

        return appointmentService.mapToResponse(appointment);
    }

    // ── Cancel ────────────────────────────────────────────────────

    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId, CancelAppointmentRequest request,
                                                   UUID performedBy, HttpServletRequest httpRequest) {
        Appointment appointment = appointmentService.findAppointmentOrThrow(appointmentId);
        appointment.setCancellationReason(request.getCancellationReason());

        User canceller = userRepository.findById(performedBy).orElse(null);
        appointment.setCancelledBy(canceller);

        appointmentService.transitionStatus(appointment, AppointmentStatus.CANCELLED, performedBy,
                "Cancelled: " + request.getCancellationReason());

        auditService.log(performedBy, "APPOINTMENT_CANCELLED", "Appointment", appointmentId,
                "Appointment cancelled: " + request.getCancellationReason(), httpRequest);

        return appointmentService.mapToResponse(appointment);
    }

    // ── No-show ───────────────────────────────────────────────────

    @Transactional
    public AppointmentResponse markNoShow(UUID appointmentId, UUID performedBy, HttpServletRequest httpRequest) {
        Appointment appointment = appointmentService.findAppointmentOrThrow(appointmentId);
        appointmentService.transitionStatus(appointment, AppointmentStatus.NO_SHOW, performedBy, "Patient did not show up");

        auditService.log(performedBy, "APPOINTMENT_NO_SHOW", "Appointment", appointmentId,
                "Patient marked as no-show", httpRequest);

        return appointmentService.mapToResponse(appointment);
    }

    // ── Add doctor notes ──────────────────────────────────────────

    @Transactional
    public AppointmentResponse addDoctorNotes(UUID appointmentId, DoctorNotesRequest request,
                                                UUID performedBy, HttpServletRequest httpRequest) {
        Appointment appointment = appointmentService.findAppointmentOrThrow(appointmentId);
        appointment.setDoctorNotes(request.getDoctorNotes());
        // appointment is already managed, will be flushed on transaction commit

        auditService.log(performedBy, "DOCTOR_NOTES_ADDED", "Appointment", appointmentId,
                "Doctor notes updated", httpRequest);

        return appointmentService.mapToResponse(appointment);
    }
}
