package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.auth.EmailAddressRequest;
import com.ogm.hrms.dto.auth.PasswordResetConfirmRequest;
import com.ogm.hrms.dto.auth.PasswordResetRequest;
import com.ogm.hrms.dto.auth.TokenConfirmRequest;
import com.ogm.hrms.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public self-service account endpoints: password reset and email verification. Request endpoints
 * always respond success (no user enumeration); confirm endpoints validate and consume the token.
 */
@Tag(name = "Account Self-Service", description = "Public password-reset and email-verification workflows.")
@RestController
@RequestMapping("/api/v1/auth")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Request password reset",
            description = "Sends a password-reset email if the address matches an account. Always responds success to avoid user enumeration. Public endpoint.")
    @PostMapping("/password-reset/request")
    public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request,
                                                  HttpServletRequest http) {
        accountService.requestPasswordReset(request.email());
        return ApiResponse.success(null, "If the account exists, a password reset email has been sent",
                http.getRequestURI());
    }

    @Operation(summary = "Confirm password reset",
            description = "Validates and consumes the reset token, then sets the new password. Public endpoint.")
    @PostMapping("/password-reset/confirm")
    public ApiResponse<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request,
                                                  HttpServletRequest http) {
        accountService.confirmPasswordReset(request.token(), request.newPassword());
        return ApiResponse.success(null, "Your password has been reset", http.getRequestURI());
    }

    @Operation(summary = "Resend email verification",
            description = "Sends a verification email if the account exists and is unverified. Always responds success to avoid user enumeration. Public endpoint.")
    @PostMapping("/email/verify/request")
    public ApiResponse<Void> resendVerification(@Valid @RequestBody EmailAddressRequest request,
                                                HttpServletRequest http) {
        accountService.resendEmailVerification(request.email());
        return ApiResponse.success(null, "If the account exists and is unverified, a verification email has been sent",
                http.getRequestURI());
    }

    @Operation(summary = "Confirm email verification",
            description = "Validates and consumes the verification token, marking the account's email as verified. Public endpoint.")
    @PostMapping("/email/verify/confirm")
    public ApiResponse<Void> confirmVerification(@Valid @RequestBody TokenConfirmRequest request,
                                                 HttpServletRequest http) {
        accountService.confirmEmailVerification(request.token());
        return ApiResponse.success(null, "Your email has been verified", http.getRequestURI());
    }
}
