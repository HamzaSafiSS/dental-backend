package com.dental.dentalbackend.notification.service;

import com.dental.dentalbackend.notification.dto.NotificationResponse;
import com.dental.dentalbackend.notification.entity.*;
import com.dental.dentalbackend.notification.repository.NotificationRepository;
import com.dental.dentalbackend.notification.template.NotificationTemplateService;
import com.dental.dentalbackend.user.entity.User;
import com.dental.dentalbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateService templateService;
    private final NotificationSender notificationSender;
    private final UserRepository userRepository;

    /**
     * Create and dispatch a notification.
     *
     * @param userId        recipient user ID
     * @param type          notification type
     * @param channel       delivery channel (SMS, EMAIL)
     * @param params        template parameters (patientName, doctorName, date, time, serviceName)
     * @param referenceType entity type being referenced (e.g. "Appointment")
     * @param referenceId   entity ID being referenced
     */
    @Transactional
    public void send(UUID userId, NotificationType type, NotificationChannel channel,
                     Map<String, Object> params, String referenceType, UUID referenceId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot send notification: user {} not found", userId);
            return;
        }

        String title = templateService.getTitle(type);
        String message = templateService.getMessage(type, params);

        // Determine recipient contact based on channel
        String recipientContact = switch (channel) {
            case EMAIL -> user.getEmail();
            case SMS -> user.getPhone();
        };

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setChannel(channel);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRecipientContact(recipientContact);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);

        // Dispatch via the configured sender (logging in dev, real provider in prod)
        try {
            notificationSender.send(notification);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    /**
     * Simplified overload defaulting to EMAIL channel.
     */
    @Transactional
    public void send(UUID userId, NotificationType type, Map<String, Object> params,
                     String referenceType, UUID referenceId) {
        send(userId, type, NotificationChannel.EMAIL, params, referenceType, referenceId);
    }

    // ── Query notifications ───────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findAllByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // ── Mapper ────────────────────────────────────────────────────

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUser().getId())
                .type(n.getType().name())
                .channel(n.getChannel().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .recipientContact(n.getRecipientContact())
                .status(n.getStatus().name())
                .sentAt(n.getSentAt())
                .errorMessage(n.getErrorMessage())
                .referenceType(n.getReferenceType())
                .referenceId(n.getReferenceId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
