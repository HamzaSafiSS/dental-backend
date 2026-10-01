package com.dental.dentalbackend.notification.service;

import com.dental.dentalbackend.notification.entity.Notification;

/**
 * Strategy interface for sending notifications via different channels.
 */
public interface NotificationSender {

    /**
     * Send the notification via this channel.
     * Implementations should update the notification's status and sentAt fields.
     */
    void send(Notification notification);
}
