package com.dental.dentalbackend.content.service;

import com.dental.dentalbackend.clinic.repository.ClinicSettingsRepository;
import com.dental.dentalbackend.content.dto.GuestReviewRequest;
import com.dental.dentalbackend.content.entity.Review;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewEmailService {

    private final JavaMailSender mailSender;
    private final ClinicSettingsRepository clinicSettingsRepository;

    @Value("${spring.mail.from:noreply@dental-clinic.com}")
    private String fromAddress;

    @Async
    public void sendNewReviewNotificationToClinic(Review review) {
        String clinicEmail = resolveClinicEmail();

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(clinicEmail);
            helper.setSubject("New Review Submitted - " + review.getPatient().getUser().getFirstName());
            helper.setText(buildClinicBody(review));

            mailSender.send(mime);
            log.info("New review notification sent to clinic ({}) for patient {}", clinicEmail, review.getPatient().getUser().getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send new review notification to clinic: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendGuestReviewNotificationToClinic(GuestReviewRequest req) {
        String clinicEmail = resolveClinicEmail();

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(clinicEmail);
            if (req.getEmail() != null && !req.getEmail().isBlank()) {
                helper.setReplyTo(req.getEmail());
            }
            helper.setSubject("New Guest Review Submitted - " + req.getName());
            helper.setText(buildGuestBody(req));

            mailSender.send(mime);
            log.info("Guest review notification sent to clinic ({}) from {}", clinicEmail, req.getName());

        } catch (MessagingException e) {
            log.error("Failed to send guest review notification to clinic: {}", e.getMessage(), e);
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

    private String buildClinicBody(Review review) {
        return """
                A new review has been submitted.
                
                ─────────────────────────────────────
                Patient Name   : %s %s
                Patient Email  : %s
                Rating         : %d / 5
                ─────────────────────────────────────
                
                Comment:
                %s
                
                ─────────────────────────────────────
                Log in to the admin dashboard to moderate this review.
                """.formatted(
                review.getPatient().getUser().getFirstName(),
                review.getPatient().getUser().getLastName(),
                review.getPatient().getUser().getEmail(),
                review.getRating(),
                review.getComment() != null ? review.getComment() : "No comment provided"
        );
    }

    private String buildGuestBody(GuestReviewRequest req) {
        return """
                A new review has been submitted by a website visitor.
                
                ─────────────────────────────────────
                Name           : %s
                Email          : %s
                Rating         : %d / 5
                Treatment      : %s
                ─────────────────────────────────────
                
                Comment:
                %s
                
                ─────────────────────────────────────
                Log in to the admin dashboard to manage reviews.
                """.formatted(
                req.getName(),
                req.getEmail() != null ? req.getEmail() : "—",
                req.getRating(),
                req.getTreatment() != null && !req.getTreatment().isBlank() ? req.getTreatment() : "Not specified",
                req.getComment()
        );
    }
}

