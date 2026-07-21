package com.ogm.hrms.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The single, standardized response envelope for every HRMS API — success and error alike
 * (decision D-007). Controllers return {@code ApiResponse<T>}; {@code GlobalExceptionHandler}
 * emits the same shape for failures, so clients parse one contract everywhere.
 *
 * <p>Null members are omitted from the JSON body, so a success payload does not carry an
 * {@code errors} array and an error payload does not carry {@code data}.</p>
 *
 * @param <T> the type of the {@code data} payload for success responses
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<FieldError> errors,
        String path,
        OffsetDateTime timestamp,
        String requestId
) {

    /** A single field-level validation failure. */
    public record FieldError(String field, String message) {}

    private static String currentRequestId() {
        return MDC.get(com.ogm.hrms.common.web.CorrelationIdFilter.MDC_KEY);
    }

    public static <T> ApiResponse<T> success(T data, String message, String path) {
        return new ApiResponse<>(true, message, data, null, path, OffsetDateTime.now(), currentRequestId());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return success(data, message, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "OK", null);
    }

    public static ApiResponse<Void> error(String message, List<FieldError> errors, String path) {
        return new ApiResponse<>(false, message, null, errors, path, OffsetDateTime.now(), currentRequestId());
    }

    public static ApiResponse<Void> error(String message, String path) {
        return error(message, null, path);
    }
}
