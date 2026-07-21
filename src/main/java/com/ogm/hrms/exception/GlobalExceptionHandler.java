package com.ogm.hrms.exception;

import com.ogm.hrms.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Centralized exception handling for all REST controllers. Every handler returns the standardized
 * {@link ApiResponse} envelope (decision D-007), so clients parse one contract for success and error.
 *
 * <p>Spring MVC exceptions are mapped to their correct HTTP status <em>before</em> the generic
 * fallback, so genuine 400/404/405 conditions are never masked as 500. Internal exception detail is
 * never leaked to clients.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean-validation failures on {@code @Valid} request bodies → 400 with a field-by-field breakdown. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<ApiResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST,
                ApiResponse.error("Validation failed for one or more fields", fieldErrors, request.getRequestURI()));
    }

    /** Domain resource lookups that miss → 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex,
                                                                    HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ApiResponse.error(ex.getMessage(), request.getRequestURI()));
    }

    /** No handler/static resource for the path → 404 (not 500). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND,
                ApiResponse.error("The requested resource was not found", request.getRequestURI()));
    }

    /** Wrong HTTP method for the path → 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                      HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                ApiResponse.error("HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
                        request.getRequestURI()));
    }

    /** Unreadable/malformed request body → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                ApiResponse.error("Malformed or unreadable request body", request.getRequestURI()));
    }

    /** A path/query parameter with the wrong type (e.g. a non-numeric id) → 400, never a 500. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                ApiResponse.error("Parameter '" + ex.getName() + "' has an invalid value", request.getRequestURI()));
    }

    /** A required query parameter is missing → 400. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                ApiResponse.error("Required parameter '" + ex.getParameterName() + "' is missing",
                        request.getRequestURI()));
    }

    /** Authenticated user lacks the required authority → 403. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN,
                ApiResponse.error("You do not have permission to perform this action", request.getRequestURI()));
    }

    /** Missing/invalid authentication → 401. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED,
                ApiResponse.error("Authentication is required or has failed", request.getRequestURI()));
    }

    /** Business/application exceptions carry their own status and a safe, client-facing message. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ApiResponse.error(ex.getMessage(), request.getRequestURI()));
    }

    /** Fallback for anything not handled more specifically → 500 (generic, no leaked detail). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                ApiResponse.error("An unexpected error occurred", request.getRequestURI()));
    }

    private ApiResponse.FieldError toFieldError(FieldError error) {
        return new ApiResponse.FieldError(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, ApiResponse<Void> body) {
        return ResponseEntity.status(status).body(body);
    }
}
