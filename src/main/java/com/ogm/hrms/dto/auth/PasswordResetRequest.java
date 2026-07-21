package com.ogm.hrms.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request a password-reset email for the given address. */
public record PasswordResetRequest(
        @NotBlank @Email String email
) {
}
