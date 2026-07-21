package com.ogm.hrms.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** A request carrying only an email address (e.g. to resend an email-verification link). */
public record EmailAddressRequest(
        @NotBlank @Email String email
) {
}
