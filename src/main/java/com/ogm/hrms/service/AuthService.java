package com.ogm.hrms.service;

import com.ogm.hrms.dto.auth.CurrentUserResponse;
import com.ogm.hrms.dto.auth.LoginRequest;
import com.ogm.hrms.dto.auth.TokenResponse;
import com.ogm.hrms.security.AuthenticatedUser;

/**
 * Authentication operations: credential login (with lockout + login history), refresh-token
 * rotation, logout (token revocation), and current-user resolution.
 */
public interface AuthService {

    TokenResponse login(LoginRequest request, String ipAddress, String userAgent);

    TokenResponse refresh(String refreshToken, String ipAddress, String userAgent);

    void logout(String refreshToken);

    CurrentUserResponse currentUser(AuthenticatedUser principal);
}
