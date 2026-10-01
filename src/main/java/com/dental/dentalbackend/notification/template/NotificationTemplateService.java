package com.dental.dentalbackend.notification.template;

import com.dental.dentalbackend.notification.entity.NotificationType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Generates notification messages from templates.
 * In production, this could be replaced by a proper template engine (Thymeleaf, FreeMarker).
 */
@Service
public class NotificationTemplateService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    public String getTitle(NotificationType type) {
        return switch (type) {
            case APPOINTMENT_REQUESTED -> "Appointment Requested";
            case PAYMENT_REQUIRED -> "Payment Required";
            case APPOINTMENT_CONFIRMED -> "Appointment Confirmed";
            case APPOINTMENT_RESCHEDULED -> "Appointment Rescheduled";
            case APPOINTMENT_CANCELLED -> "Appointment Cancelled";
            case APPOINTMENT_REMINDER -> "Appointment Reminder";
            case PAYMENT_VERIFIED -> "Payment Verified";
            case PAYMENT_REJECTED -> "Payment Rejected";
        };
    }

    public String getMessage(NotificationType type, Map<String, Object> params) {
        String patientName = (String) params.getOrDefault("patientName", "Patient");
        String doctorName = (String) params.getOrDefault("doctorName", "Doctor");
        String serviceName = (String) params.getOrDefault("serviceName", "Service");
        LocalDate date = (LocalDate) params.get("date");
        LocalTime time = (LocalTime) params.get("time");

        String dateStr = date != null ? date.format(DATE_FMT) : "";
        String timeStr = time != null ? time.format(TIME_FMT) : "";

        return switch (type) {
            case APPOINTMENT_REQUESTED -> String.format(
                    "Dear %s, your appointment for %s with Dr. %s on %s at %s has been requested. " +
                    "Please submit your payment to confirm the booking.",
                    patientName, serviceName, doctorName, dateStr, timeStr);

            case PAYMENT_REQUIRED -> String.format(
                    "Dear %s, payment is required for your appointment on %s at %s. " +
                    "Please submit your payment proof to proceed.",
                    patientName, dateStr, timeStr);

            case APPOINTMENT_CONFIRMED -> String.format(
                    "Dear %s, your appointment with Dr. %s on %s at %s has been confirmed. " +
                    "Please arrive 10 minutes before your scheduled time.",
                    patientName, doctorName, dateStr, timeStr);

            case APPOINTMENT_RESCHEDULED -> String.format(
                    "Dear %s, your appointment has been rescheduled to %s at %s with Dr. %s.",
                    patientName, dateStr, timeStr, doctorName);

            case APPOINTMENT_CANCELLED -> String.format(
                    "Dear %s, your appointment on %s at %s has been cancelled. " +
                    "Please contact us if you have any questions.",
                    patientName, dateStr, timeStr);

            case APPOINTMENT_REMINDER -> String.format(
                    "Reminder: Dear %s, you have an appointment with Dr. %s tomorrow (%s) at %s.",
                    patientName, doctorName, dateStr, timeStr);

            case PAYMENT_VERIFIED -> String.format(
                    "Dear %s, your payment for the appointment on %s has been verified. " +
                    "Your appointment will be confirmed shortly.",
                    patientName, dateStr);

            case PAYMENT_REJECTED -> String.format(
                    "Dear %s, your payment for the appointment on %s was not approved. " +
                    "Please resubmit a valid payment proof.",
                    patientName, dateStr);
        };
    }
}
