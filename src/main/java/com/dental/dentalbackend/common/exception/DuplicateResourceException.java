package com.dental.dentalbackend.common.exception;

import lombok.Getter;

/**
 * Thrown when attempting to create a resource that already exists
 * (e.g., duplicate email during registration).
 * Results in HTTP 409 Conflict.
 */
@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
