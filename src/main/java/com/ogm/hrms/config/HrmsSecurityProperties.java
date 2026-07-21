package com.ogm.hrms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * Externalized, environment-driven security configuration (never hardcoded). The JWT signing secret
 * must be supplied via the environment ({@code HRMS_JWT_SECRET}); TTLs, lockout policy, and CORS
 * origins have safe defaults that can be tuned per environment.
 */
@ConfigurationProperties(prefix = "hrms.security")
public record HrmsSecurityProperties(
        @DefaultValue Jwt jwt,
        @DefaultValue Lockout lockout,
        @DefaultValue Tokens tokens,
        @DefaultValue Cors cors
) {

    public record Jwt(
            String secret,
            @DefaultValue("PT15M") Duration accessTokenTtl,
            @DefaultValue("P7D") Duration refreshTokenTtl,
            @DefaultValue("ogm-hrms") String issuer
    ) {}

    public record Lockout(
            @DefaultValue("5") int maxFailedAttempts,
            @DefaultValue("PT15M") Duration lockDuration
    ) {}

    /** Time-to-live for single-use account tokens. */
    public record Tokens(
            @DefaultValue("PT30M") Duration passwordResetTtl,
            @DefaultValue("P1D") Duration emailVerificationTtl
    ) {}

    /**
     * Cross-Origin Resource Sharing policy for the {@code /api/**} surface, so browser clients on a
     * different origin (e.g. the SPA dev server) can call the API. Safe by default: with no
     * {@code allowedOrigins} configured, CORS is not applied at all and no cross-origin access is
     * granted. Origins are supplied per environment (never a wildcard in production).
     */
    public record Cors(
            @DefaultValue List<String> allowedOrigins,
            @DefaultValue({"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"}) List<String> allowedMethods,
            @DefaultValue({"Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"})
            List<String> allowedHeaders,
            @DefaultValue({"X-Request-Id"}) List<String> exposedHeaders,
            @DefaultValue("false") boolean allowCredentials,
            @DefaultValue("PT30M") Duration maxAge
    ) {}
}
