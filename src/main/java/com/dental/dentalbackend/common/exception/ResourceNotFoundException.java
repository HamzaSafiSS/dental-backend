package com.dental.dentalbackend.common.exception;

import lombok.Getter;

/**
 * Thrown when a requested resource does not exist in the database.
 * Results in HTTP 404 Not Found.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        this(resourceName, "id", id);
    }
}
