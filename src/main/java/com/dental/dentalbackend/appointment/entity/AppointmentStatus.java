package com.dental.dentalbackend.appointment.entity;

public enum AppointmentStatus {
    PENDING_PAYMENT,
    PAYMENT_VERIFIED,
    CONFIRMED,
    CHECKED_IN,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    RESCHEDULED
}
