package com.ogm.hrms.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** Carries an opaque refresh token to renew or revoke a session. */
public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {
}
