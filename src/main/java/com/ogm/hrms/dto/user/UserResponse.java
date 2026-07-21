package com.ogm.hrms.dto.user;

import java.time.OffsetDateTime;
import java.util.Set;

/** Administrative view of a user account. */
public record UserResponse(
        Long id,
        String email,
        String fullName,
        boolean enabled,
        boolean accountNonLocked,
        boolean emailVerified,
        Set<String> roles,
        OffsetDateTime createdAt,
        OffsetDateTime lastLoginAt
) {
}
