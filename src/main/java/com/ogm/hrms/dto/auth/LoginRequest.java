package com.ogm.hrms.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credentials submitted to the login endpoint. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
