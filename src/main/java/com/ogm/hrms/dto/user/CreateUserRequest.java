package com.ogm.hrms.dto.user;

import com.ogm.hrms.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Request to create a user account and assign one or more roles. */
public record CreateUserRequest(
        @NotBlank @Email @Size(max = 190) String email,
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotEmpty Set<RoleName> roles
) {
}
