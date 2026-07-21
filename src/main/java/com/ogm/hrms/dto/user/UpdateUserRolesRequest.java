package com.ogm.hrms.dto.user;

import com.ogm.hrms.enums.RoleName;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/** Request to replace the set of roles assigned to a user. */
public record UpdateUserRolesRequest(
        @NotEmpty Set<RoleName> roles
) {
}
