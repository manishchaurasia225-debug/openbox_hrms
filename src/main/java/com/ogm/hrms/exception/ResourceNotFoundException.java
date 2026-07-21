package com.ogm.hrms.exception;

/**
 * Thrown when a requested domain resource cannot be found.
 *
 * <p>Handled centrally by {@link GlobalExceptionHandler} and translated into an
 * HTTP 404 response.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
