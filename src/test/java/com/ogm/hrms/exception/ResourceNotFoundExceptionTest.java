package com.ogm.hrms.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for {@link ResourceNotFoundException} message construction. */
class ResourceNotFoundExceptionTest {

    @Test
    void singleArgConstructor_usesMessageVerbatim() {
        ResourceNotFoundException ex = new ResourceNotFoundException("nothing here");

        assertThat(ex.getMessage()).isEqualTo("nothing here");
    }

    @Test
    void threeArgConstructor_formatsResourceFieldAndValue() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Employee", "id", 42);

        assertThat(ex.getMessage()).isEqualTo("Employee not found with id: '42'");
    }
}
