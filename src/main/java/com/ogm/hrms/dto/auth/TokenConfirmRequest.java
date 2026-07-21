package com.ogm.hrms.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** Confirm an account flow (e.g. email verification) using a single-use token. */
public record TokenConfirmRequest(
        @NotBlank String token
) {
}
