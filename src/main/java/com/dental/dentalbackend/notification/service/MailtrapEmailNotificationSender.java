package com.dental.dentalbackend.notification.service;

import com.dental.dentalbackend.notification.entity.Notification;
import com.dental.dentalbackend.notification.entity.NotificationStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Production {@link NotificationSender} that delivers emails via Mailtrap SMTP
 * using Spring's built-in {@link JavaMailSender} — exactly the same pattern
 * used in rent-collector's {@code EmailService}.
 *
 * Marked {@code @Primary} so Spring injects this wherever {@code NotificationSender}
 * is needed, automatically taking precedence over {@link LoggingNotificationSender}
 * (the dev-only logging fallback).
 *
 * SMTP credentials come from environment variables (set in .env):
 *   SMTP_HOST — e.g. sandbox.smtp.mailtrap.io
 *   SMTP_PORT — e.g. 2525
 *   SMTP_USER — your Mailtrap SMTP username
 *   SMTP_PASS — your Mailtrap SMTP password
 *   MAIL_FROM — (optional) sender address, defaults to noreply@dental-clinic.com
 *
 * Monitor sent emails at: https://mailtrap.io/sending/email_logs
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class MailtrapEmailNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;

    /** Injected from spring.mail.from, which is backed by the MAIL_FROM env var. */
    @Value("${spring.mail.from:noreply@dental-clinic.com}")
    private String fromAddress;

    @Override
    public void send(Notification notification) {
        String recipientEmail = notification.getRecipientContact();

        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("Skipping email notification {}: recipient email is blank", notification.getId());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage("Recipient email address is missing.");
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject(notification.getTitle());
            helper.setText(notification.getMessage());

            mailSender.send(mimeMessage);

            log.info("Email sent via Mailtrap SMTP to {} — type={}, notificationId={}",
                    recipientEmail, notification.getType(), notification.getId());
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());

        } catch (MailException | MessagingException e) {
            log.error("SMTP delivery failed for notification {}: {}", notification.getId(), e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            throw new RuntimeException("Email delivery failed: " + e.getMessage(), e);
        }
    }
}
