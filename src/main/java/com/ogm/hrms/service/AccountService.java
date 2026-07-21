package com.ogm.hrms.service;

/**
 * Self-service account flows that rely on single-use, hashed tokens delivered by email: password
 * reset and email verification. Request operations never reveal whether an account exists (to avoid
 * user enumeration); confirm operations validate and consume the token.
 */
public interface AccountService {

    void requestPasswordReset(String email);

    void confirmPasswordReset(String rawToken, String newPassword);

    void resendEmailVerification(String email);

    void confirmEmailVerification(String rawToken);
}
