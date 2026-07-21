package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.auth.CurrentUserResponse;
import com.ogm.hrms.dto.auth.LoginRequest;
import com.ogm.hrms.dto.auth.RefreshTokenRequest;
import com.ogm.hrms.dto.auth.TokenResponse;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints (public: login/refresh/logout; authenticated: current user). All
 * responses use the standard {@link ApiResponse} envelope.
 */
@Tag(name = "Authentication", description = "Login, token refresh, logout, and current-user identity endpoints.")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Authenticate and issue tokens",
            description = "Validates credentials and returns access and refresh tokens. Public endpoint; records client IP and user agent.")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        TokenResponse tokens = authService.login(request, clientIp(http), http.getHeader("User-Agent"));
        return ApiResponse.success(tokens, "Login successful", http.getRequestURI());
    }

    @Operation(summary = "Refresh access token",
            description = "Exchanges a valid refresh token for a new access/refresh token pair. Public endpoint.")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest http) {
        TokenResponse tokens = authService.refresh(request.refreshToken(), clientIp(http), http.getHeader("User-Agent"));
        return ApiResponse.success(tokens, "Token refreshed", http.getRequestURI());
    }

    @Operation(summary = "Log out",
            description = "Revokes the supplied refresh token so it can no longer be used. Public endpoint.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest http) {
        authService.logout(request.refreshToken());
        return ApiResponse.success(null, "Logged out", http.getRequestURI());
    }

    @Operation(summary = "Get current user",
            description = "Returns the profile, roles, and permissions of the authenticated caller. Requires a valid access token.")
    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal AuthenticatedUser principal,
                                               HttpServletRequest http) {
        return ApiResponse.success(authService.currentUser(principal), "OK", http.getRequestURI());
    }

    /** Best-effort client IP, honouring a proxy's {@code X-Forwarded-For} first entry. */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
