package com.dental.dentalbackend.common.exception;

/**
 * Thrown when a business rule is violated.
 * Examples: confirming appointment without verified payment,
 * booking a slot that conflicts with existing appointments.
 * Results in HTTP 400 Bad Request.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
