package com.dental.dentalbackend.contact.service;

import com.dental.dentalbackend.clinic.repository.ClinicSettingsRepository;
import com.dental.dentalbackend.contact.dto.ContactRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends two emails when a visitor submits the contact form:
 *
 *  1. To the clinic — forwarded message with all visitor details.
 *  2. To the visitor — auto-reply confirming their message was received.
 *
 * The clinic's email address is fetched from ClinicSettings so it can be
 * changed via the admin panel without touching code.
 *
 * This is an @Async fire-and-forget — the HTTP response is returned
 * immediately while Spring sends the emails in the background.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactEmailService {

    private final JavaMailSender mailSender;
    private final ClinicSettingsRepository clinicSettingsRepository;

    @Value("${spring.mail.from:noreply@dental-clinic.com}")
    private String fromAddress;

    /**
     * Forward the visitor's contact form to the clinic and send an auto-reply.
     */
    @Async
    public void sendContactEmails(ContactRequest req) {
        String clinicEmail = resolveClinicEmail();

        sendClinicNotification(req, clinicEmail);
        sendVisitorAutoReply(req);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Fetch the clinic's configured email, fallback to the MAIL_FROM sender address. */
    private String resolveClinicEmail() {
        try {
            String email = clinicSettingsRepository.getSettings().getEmail();
            return (email != null && !email.isBlank()) ? email : fromAddress;
        } catch (Exception e) {
            log.warn("Could not fetch clinic email from settings, using default sender: {}", e.getMessage());
            return fromAddress;
        }
    }

    /** Email sent TO the clinic — contains all form details. */
    private void sendClinicNotification(ContactRequest req, String clinicEmail) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(clinicEmail);
            helper.setReplyTo(req.getEmail());   // "Reply" in the inbox goes straight to the visitor
            helper.setSubject("[Contact Form] " + req.getSubject());
            helper.setText(buildClinicBody(req));

            mailSender.send(mime);
            log.info("Contact form forwarded to clinic ({}) from {}", clinicEmail, req.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send clinic notification email: {}", e.getMessage(), e);
        }
    }

    /** Auto-reply email sent TO the visitor confirming receipt. */
    private void sendVisitorAutoReply(ContactRequest req) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(req.getEmail());
            helper.setSubject("We received your message — Bright Smiles Dental Care");
            helper.setText(buildAutoReplyBody(req));

            mailSender.send(mime);
            log.info("Auto-reply sent to visitor: {}", req.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send auto-reply email to {}: {}", req.getEmail(), e.getMessage(), e);
        }
    }

    private String buildClinicBody(ContactRequest req) {
        return """
                New contact form submission received from the website.

                ─────────────────────────────────────
                Name    : %s
                Phone   : %s
                Email   : %s
                Subject : %s
                ─────────────────────────────────────

                Message:
                %s

                ─────────────────────────────────────
                Reply directly to this email to respond to the visitor.
                """.formatted(
                req.getName(),
                req.getPhone() != null ? req.getPhone() : "—",
                req.getEmail(),
                req.getSubject(),
                req.getMessage()
        );
    }

    private String buildAutoReplyBody(ContactRequest req) {
        return """
                Dear %s,

                Thank you for reaching out to Bright Smiles Dental Care.

                We have received your message and our team will get back to you
                as soon as possible, usually within 1 business day.

                If your matter is urgent, please call us directly at +251 96 637 7639.

                Best regards,
                Bright Smiles Dental Care Team
                """.formatted(
                req.getName()
        );
    }
}
