package com.dental.dentalbackend.notification.service;

import com.dental.dentalbackend.notification.entity.Notification;
import com.dental.dentalbackend.notification.entity.NotificationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Development fallback — logs notifications to console instead of sending externally.
 * This is the default sender used when no real SMS/email provider is configured.
 */
@Slf4j
@Component
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public void send(Notification notification) {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║ NOTIFICATION [{}] via {}",
                notification.getType(), notification.getChannel());
        log.info("║ To: {} ({})",
                notification.getRecipientContact(), notification.getUser().getEmail());
        log.info("║ Title: {}", notification.getTitle());
        log.info("║ Message: {}", notification.getMessage());
        if (notification.getReferenceType() != null) {
            log.info("║ Ref: {} / {}", notification.getReferenceType(), notification.getReferenceId());
        }
        log.info("╚══════════════════════════════════════════════════════════╝");

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
    }
}
