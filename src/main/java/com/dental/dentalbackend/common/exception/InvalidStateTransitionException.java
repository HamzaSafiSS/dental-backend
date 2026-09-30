package com.dental.dentalbackend.common.exception;

import lombok.Getter;

/**
 * Thrown when an entity state transition is invalid.
 * Examples: PENDING_PAYMENT → COMPLETED, PENDING_PAYMENT → CONFIRMED without payment verification.
 * Results in HTTP 409 Conflict.
 */
@Getter
public class InvalidStateTransitionException extends RuntimeException {

    private final String entityType;
    private final String currentState;
    private final String targetState;

    public InvalidStateTransitionException(String entityType, String currentState, String targetState) {
        super(String.format("Invalid %s state transition: %s -> %s", entityType, currentState, targetState));
        this.entityType = entityType;
        this.currentState = currentState;
        this.targetState = targetState;
    }
}
