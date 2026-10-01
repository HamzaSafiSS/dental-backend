package com.dental.dentalbackend.appointment.statemachine;

import com.dental.dentalbackend.appointment.entity.AppointmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentStateMachineTest {

    @Test
    @DisplayName("PENDING_PAYMENT transitions only to PAYMENT_VERIFIED and CANCELLED")
    void pendingPaymentTransitions() {
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.PENDING_PAYMENT, AppointmentStatus.PAYMENT_VERIFIED));
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.PENDING_PAYMENT, AppointmentStatus.CANCELLED));

        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.PENDING_PAYMENT, AppointmentStatus.CONFIRMED));
        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.PENDING_PAYMENT, AppointmentStatus.CHECKED_IN));
        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.PENDING_PAYMENT, AppointmentStatus.IN_PROGRESS));
        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.PENDING_PAYMENT, AppointmentStatus.COMPLETED));
    }

    @Test
    @DisplayName("PAYMENT_VERIFIED transitions to CONFIRMED and CANCELLED")
    void paymentVerifiedTransitions() {
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.PAYMENT_VERIFIED, AppointmentStatus.CONFIRMED));
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.PAYMENT_VERIFIED, AppointmentStatus.CANCELLED));

        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.PAYMENT_VERIFIED, AppointmentStatus.CHECKED_IN));
        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.PAYMENT_VERIFIED, AppointmentStatus.COMPLETED));
    }

    @Test
    @DisplayName("CONFIRMED transitions to CHECKED_IN, CANCELLED, NO_SHOW, RESCHEDULED")
    void confirmedTransitions() {
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.CONFIRMED, AppointmentStatus.CHECKED_IN));
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED));
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.CONFIRMED, AppointmentStatus.NO_SHOW));
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULED));

        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED));
        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("CHECKED_IN transitions only to IN_PROGRESS")
    void checkedInTransitions() {
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS));

        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.CHECKED_IN, AppointmentStatus.COMPLETED));
        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.CHECKED_IN, AppointmentStatus.CANCELLED));
    }

    @Test
    @DisplayName("IN_PROGRESS transitions only to COMPLETED")
    void inProgressTransitions() {
        assertTrue(AppointmentStateMachine.canTransition(
                AppointmentStatus.IN_PROGRESS, AppointmentStatus.COMPLETED));

        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.IN_PROGRESS, AppointmentStatus.CANCELLED));
        assertFalse(AppointmentStateMachine.canTransition(
                AppointmentStatus.IN_PROGRESS, AppointmentStatus.CHECKED_IN));
    }

    @Test
    @DisplayName("Terminal states allow no further transitions")
    void terminalStatesAllowNoTransitions() {
        AppointmentStatus[] terminalStates = {
                AppointmentStatus.COMPLETED,
                AppointmentStatus.CANCELLED,
                AppointmentStatus.NO_SHOW,
                AppointmentStatus.RESCHEDULED
        };

        for (AppointmentStatus terminal : terminalStates) {
            assertTrue(AppointmentStateMachine.allowedTransitions(terminal).isEmpty(),
                    terminal + " should have no outgoing transitions");

            for (AppointmentStatus anyStatus : AppointmentStatus.values()) {
                assertFalse(AppointmentStateMachine.canTransition(terminal, anyStatus),
                        "Should not be able to transition from " + terminal + " to " + anyStatus);
            }
        }
    }
}
