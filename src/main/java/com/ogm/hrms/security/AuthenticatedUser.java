package com.ogm.hrms.security;

import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * The authentication principal placed in the security context for API requests. Carries the minimum
 * identity needed by controllers/services (user id + email); authorities live on the
 * {@code Authentication}, not here.
 *
 * <p>Implements {@link AuthenticatedPrincipal} so {@code Authentication.getName()} resolves to the
 * user's email. This keeps the name stable and human-readable everywhere Spring reads it — notably
 * JPA auditing ({@code @CreatedBy}/{@code @LastModifiedBy}), which would otherwise persist the
 * record's {@code toString()} into the {@code created_by}/{@code updated_by} columns.</p>
 */
public record AuthenticatedUser(Long id, String email) implements AuthenticatedPrincipal {

    @Override
    public String getName() {
        return email;
    }
}
