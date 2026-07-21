package com.ogm.hrms.exception;

import org.springframework.http.HttpStatus;

/**
 * A business/application exception that carries the HTTP status it should map to. Thrown by services
 * to signal a client-facing failure (e.g. invalid credentials, conflict, forbidden business rule)
 * with a safe, user-appropriate message. Translated centrally by {@code GlobalExceptionHandler}.
 */
public class ApiException extends RuntimeException {

    private final transient HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException locked(String message) {
        return new ApiException(HttpStatus.LOCKED, message);
    }
}
