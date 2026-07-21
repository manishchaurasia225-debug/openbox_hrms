package com.ogm.hrms.dto.auth;

import java.util.Set;

/** Identity view of the authenticated user, including resolved roles and permission authorities. */
public record CurrentUserResponse(
        Long id,
        String email,
        String fullName,
        Set<String> roles,
        Set<String> authorities
) {
}
