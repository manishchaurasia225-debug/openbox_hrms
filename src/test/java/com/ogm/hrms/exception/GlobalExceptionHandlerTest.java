package com.ogm.hrms.exception;

import com.ogm.hrms.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalExceptionHandler}. The handler is a plain object exercised directly
 * with a {@link MockHttpServletRequest} — no Spring context needed. Every handler must return the
 * standardized {@link ApiResponse} envelope with {@code success=false}.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returns404WithMessageAndPath() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/employees/99");
        ResourceNotFoundException ex = new ResourceNotFoundException("Employee", "id", 99);

        ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ApiResponse<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isFalse();
        assertThat(body.message()).isEqualTo("Employee not found with id: '99'");
        assertThat(body.path()).isEqualTo("/api/employees/99");
        assertThat(body.errors()).isNull();
        assertThat(body.data()).isNull();
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void handleGeneric_returns500AndDoesNotLeakInternalDetail() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/boom");

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleGeneric(new RuntimeException("sensitive internal detail"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiResponse<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isFalse();
        assertThat(body.message()).isEqualTo("An unexpected error occurred");
        assertThat(body.message()).doesNotContain("sensitive internal detail");
        assertThat(body.path()).isEqualTo("/api/boom");
    }

    @Test
    void handleValidation_returns400WithPerFieldErrors() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/employees");

        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "employee");
        bindingResult.addError(new FieldError("employee", "email", "must not be blank"));

        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("validationTarget", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isFalse();
        assertThat(body.message()).isEqualTo("Validation failed for one or more fields");
        assertThat(body.errors()).containsExactly(new ApiResponse.FieldError("email", "must not be blank"));
        assertThat(body.path()).isEqualTo("/api/employees");
    }

    @Test
    void handleTypeMismatch_returns400NotServerError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/employees/abc");

        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("validationTarget", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", parameter, new NumberFormatException("For input string: \"abc\""));

        ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isFalse();
        assertThat(body.message()).isEqualTo("Parameter 'id' has an invalid value");
        assertThat(body.path()).isEqualTo("/api/v1/employees/abc");
    }

    /** Reflection target used only to build a {@link MethodParameter} for the validation test. */
    @SuppressWarnings("unused")
    private void validationTarget(String value) {
        // intentionally empty
    }
}
