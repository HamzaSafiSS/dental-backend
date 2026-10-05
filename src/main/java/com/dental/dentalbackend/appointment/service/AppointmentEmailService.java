package com.dental.dentalbackend.appointment.service;

import com.dental.dentalbackend.appointment.dto.GuestAppointmentRequest;
import com.dental.dentalbackend.appointment.entity.Appointment;
import com.dental.dentalbackend.clinic.repository.ClinicSettingsRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;


@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentEmailService {

    private final JavaMailSender mailSender;
    private final ClinicSettingsRepository clinicSettingsRepository;

    @Value("${spring.mail.from:noreply@dental-clinic.com}")
    private String fromAddress;

    @Async
    public void sendNewAppointmentNotificationToClinic(Appointment appointment) {
        String clinicEmail = resolveClinicEmail();

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(clinicEmail);
            helper.setSubject("New Appointment Booking - " + appointment.getPatient().getUser().getFirstName());
            helper.setText(buildClinicBody(appointment));

            mailSender.send(mime);
            log.info("New appointment notification sent to clinic ({}) for patient {}", clinicEmail, appointment.getPatient().getUser().getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send new appointment notification to clinic: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendGuestAppointmentRequestToClinic(GuestAppointmentRequest req) {
        String clinicEmail = resolveClinicEmail();

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(clinicEmail);
            helper.setReplyTo(req.getEmail());
            helper.setSubject("New Public Appointment Request - " + req.getFullName());
            helper.setText(buildGuestClinicBody(req));

            mailSender.send(mime);
            log.info("Public appointment request sent to clinic ({}) for visitor {}", clinicEmail, req.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send public appointment request to clinic: {}", e.getMessage(), e);
        }
    }

    private String resolveClinicEmail() {
        try {
            String email = clinicSettingsRepository.getSettings().getEmail();
            return (email != null && !email.isBlank()) ? email : fromAddress;
        } catch (Exception e) {
            log.warn("Could not fetch clinic email from settings, using default sender: {}", e.getMessage());
            return fromAddress;
        }
    }

    private String buildClinicBody(Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        return """
                A new appointment has been booked via the website.

                ─────────────────────────────────────
                Patient Name   : %s %s
                Patient Email  : %s
                Patient Phone  : %s
                Doctor         : Dr. %s %s
                Service        : %s
                Date           : %s
                Time           : %s
                Status         : %s
                ─────────────────────────────────────

                Reason for visit:
                %s

                ─────────────────────────────────────
                Log in to the admin dashboard to view or manage this appointment.
                """.formatted(
                appointment.getPatient().getUser().getFirstName(),
                appointment.getPatient().getUser().getLastName(),
                appointment.getPatient().getUser().getEmail(),
                appointment.getPatient().getUser().getPhone() != null ? appointment.getPatient().getUser().getPhone() : "—",
                appointment.getDoctor().getUser().getFirstName(),
                appointment.getDoctor().getUser().getLastName(),
                appointment.getService().getName(),
                appointment.getAppointmentDate().format(dateFormatter),
                appointment.getStartTime().format(timeFormatter),
                appointment.getStatus().name(),
                appointment.getReasonForVisit() != null ? appointment.getReasonForVisit() : "None provided"
        );
    }

    private String buildGuestClinicBody(GuestAppointmentRequest req) {
        return """
                A new appointment REQUEST has been submitted by an unauthenticated visitor.
                You may need to contact them to confirm and officially schedule this in the system.

                ─────────────────────────────────────
                Patient Name   : %s
                Patient Email  : %s
                Patient Phone  : %s
                Date of Birth  : %s
                Patient Type   : %s
                Contact Pref.  : %s
                ─────────────────────────────────────
                Requested Date : %s
                Requested Time : %s
                Doctor         : %s
                Service        : %s
                ─────────────────────────────────────

                Reason for visit:
                %s

                ─────────────────────────────────────
                Reply directly to this email to contact the patient.
                """.formatted(
                req.getFullName(),
                req.getEmail(),
                req.getPhone() != null ? req.getPhone() : "—",
                req.getDob() != null ? req.getDob() : "—",
                "returning".equalsIgnoreCase(req.getPatientType()) ? "Returning Patient" : "New Patient",
                req.getContactMethod(),
                req.getDate(),
                req.getTime(),
                req.getDentistName() != null ? req.getDentistName() : "Any Available",
                req.getService(),
                req.getReason() != null && !req.getReason().isBlank() ? req.getReason() : "None provided"
        );
    }
}
