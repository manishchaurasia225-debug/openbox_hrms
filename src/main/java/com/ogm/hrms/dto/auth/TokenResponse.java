package com.ogm.hrms.dto.auth;

/** Issued token pair plus the identity of the authenticated user. */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        CurrentUserResponse user
) {
    public static TokenResponse bearer(String accessToken, String refreshToken, long expiresInSeconds,
                                       CurrentUserResponse user) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, user);
    }
}
