package com.dental.dentalbackend.appointment.statemachine;

import com.dental.dentalbackend.appointment.entity.AppointmentStatus;

import java.util.Map;
import java.util.Set;

/**
 * Defines the valid state transitions for appointment status.
 * This is the single source of truth for the appointment lifecycle.
 */
public final class AppointmentStateMachine {

    private AppointmentStateMachine() {}

    private static final Map<AppointmentStatus, Set<AppointmentStatus>> TRANSITIONS = Map.of(
            AppointmentStatus.PENDING_PAYMENT, Set.of(
                    AppointmentStatus.PAYMENT_VERIFIED,
                    AppointmentStatus.CANCELLED
            ),
            AppointmentStatus.PAYMENT_VERIFIED, Set.of(
                    AppointmentStatus.CONFIRMED,
                    AppointmentStatus.CANCELLED
            ),
            AppointmentStatus.CONFIRMED, Set.of(
                    AppointmentStatus.CHECKED_IN,
                    AppointmentStatus.CANCELLED,
                    AppointmentStatus.NO_SHOW,
                    AppointmentStatus.RESCHEDULED
            ),
            AppointmentStatus.CHECKED_IN, Set.of(
                    AppointmentStatus.IN_PROGRESS
            ),
            AppointmentStatus.IN_PROGRESS, Set.of(
                    AppointmentStatus.COMPLETED
            ),
            // Terminal states — no further transitions
            AppointmentStatus.COMPLETED, Set.of(),
            AppointmentStatus.CANCELLED, Set.of(),
            AppointmentStatus.NO_SHOW, Set.of(),
            AppointmentStatus.RESCHEDULED, Set.of()
    );

    /**
     * Returns true if transitioning from {@code from} to {@code to} is allowed.
     */
    public static boolean canTransition(AppointmentStatus from, AppointmentStatus to) {
        Set<AppointmentStatus> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    /**
     * Returns the set of valid target statuses for the given current status.
     */
    public static Set<AppointmentStatus> allowedTransitions(AppointmentStatus from) {
        return TRANSITIONS.getOrDefault(from, Set.of());
    }
}
