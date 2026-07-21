package com.ogm.hrms.config;

import com.ogm.hrms.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the auditor resolution used by {@code @CreatedBy}/{@code @LastModifiedBy}. Guards
 * against regressing to persisting the principal's {@code toString()} into the audit columns.
 */
class JpaAuditingConfigTest {

    private final AuditorAware<String> auditor = new JpaAuditingConfig().auditorAware();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unauthenticatedContextIsAttributedToSystem() {
        assertThat(auditor.getCurrentAuditor()).contains(JpaAuditingConfig.SYSTEM_AUDITOR);
    }

    @Test
    void authenticatedUserIsRecordedByEmailNotToString() {
        AuthenticatedUser principal = new AuthenticatedUser(3L, "admin@opengrey.media");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));

        assertThat(auditor.getCurrentAuditor()).contains("admin@opengrey.media");
    }
}
