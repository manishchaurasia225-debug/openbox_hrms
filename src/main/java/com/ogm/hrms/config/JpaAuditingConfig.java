package com.ogm.hrms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Enables Spring Data JPA auditing and supplies the current auditor for {@code @CreatedBy} /
 * {@code @LastModifiedBy} on {@link com.ogm.hrms.common.BaseEntity}.
 *
 * <p>Resolves the auditor from the security context; unauthenticated/background operations
 * (bootstrap, scheduled jobs) are attributed to {@code "system"}. A {@link DateTimeProvider} that
 * yields {@link OffsetDateTime} is supplied so the {@code timestamptz} audit columns are populated
 * correctly (the default provider yields {@code LocalDateTime}, which cannot be converted).</p>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    public static final String SYSTEM_AUDITOR = "system";

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
                return Optional.of(SYSTEM_AUDITOR);
            }
            return Optional.of(authentication.getName());
        };
    }
}
