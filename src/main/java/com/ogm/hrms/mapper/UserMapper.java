package com.ogm.hrms.mapper;

import com.ogm.hrms.dto.user.UserResponse;
import com.ogm.hrms.entity.User;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps {@link User} entities to their administrative {@link UserResponse} DTO. Must be invoked within
 * a transaction so the lazy {@code roles} association is available.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.isEmailVerified(),
                roleNames,
                user.getCreatedAt(),
                user.getLastLoginAt());
    }
}
