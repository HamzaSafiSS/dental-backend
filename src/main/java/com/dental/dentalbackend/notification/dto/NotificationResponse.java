package com.dental.dentalbackend.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private String type;
    private String channel;
    private String title;
    private String message;
    private String recipientContact;
    private String status;
    private LocalDateTime sentAt;
    private String errorMessage;
    private String referenceType;
    private UUID referenceId;
    private LocalDateTime createdAt;
}
