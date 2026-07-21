package com.ogm.hrms.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for the standardized {@link ApiResponse} envelope factories. */
class ApiResponseTest {

    @Test
    void success_setsSuccessTrueAndCarriesData() {
        ApiResponse<String> r = ApiResponse.success("payload", "Created", "/api/x");

        assertThat(r.success()).isTrue();
        assertThat(r.message()).isEqualTo("Created");
        assertThat(r.data()).isEqualTo("payload");
        assertThat(r.path()).isEqualTo("/api/x");
        assertThat(r.errors()).isNull();
        assertThat(r.timestamp()).isNotNull();
    }

    @Test
    void error_setsSuccessFalseAndCarriesFieldErrors() {
        ApiResponse<Void> r = ApiResponse.error("Validation failed",
                List.of(new ApiResponse.FieldError("email", "invalid")), "/api/y");

        assertThat(r.success()).isFalse();
        assertThat(r.data()).isNull();
        assertThat(r.errors()).containsExactly(new ApiResponse.FieldError("email", "invalid"));
        assertThat(r.path()).isEqualTo("/api/y");
        assertThat(r.timestamp()).isNotNull();
    }

    @Test
    void error_withoutFieldErrors_leavesErrorsNull() {
        ApiResponse<Void> r = ApiResponse.error("Not found", "/api/z");

        assertThat(r.success()).isFalse();
        assertThat(r.errors()).isNull();
        assertThat(r.message()).isEqualTo("Not found");
    }
}
