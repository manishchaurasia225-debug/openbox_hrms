package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.user.CreateUserRequest;
import com.ogm.hrms.dto.user.UserResponse;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.mapper.UserMapper;
import com.ogm.hrms.repository.RefreshTokenRepository;
import com.ogm.hrms.repository.RoleRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Default {@link UserService}. Enforces that only a Super Admin may assign administrative/HR roles
 * (business rule "only Super Admin can create HR"), rejects duplicate emails, and revokes all
 * refresh tokens when an account is disabled or deleted (defence in depth).
 */
@Service
public class UserServiceImpl implements UserService {

    /** Roles whose assignment is restricted to a Super Admin. */
    private static final Set<RoleName> PRIVILEGED_ROLES =
            EnumSet.of(RoleName.SUPER_ADMIN, RoleName.COMPANY_ADMIN, RoleName.HR_MANAGER, RoleName.HR_EXECUTIVE);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("A user with this email already exists");
        }
        Set<Role> roles = resolveRoles(request.roles());
        assertMayAssign(request.roles());

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setEnabled(true);
        user.setEmailVerified(false);
        roles.forEach(user::addRole);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(Pageable pageable) {
        return PageResponse.of(userRepository.findByDeletedFalse(pageable), userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userMapper.toResponse(loadActive(id));
    }

    @Override
    @Transactional
    public UserResponse updateRoles(Long id, Set<RoleName> roleNames) {
        assertMayAssign(roleNames);
        User user = loadActive(id);
        Set<Role> roles = resolveRoles(roleNames);
        user.getRoles().clear();
        roles.forEach(user::addRole);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = loadActive(id);
        user.setEnabled(enabled);
        if (!enabled) {
            refreshTokenRepository.revokeAllForUser(user); // force re-authentication / logout everywhere
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = loadActive(id);
        user.setDeleted(true);
        user.setDeletedAt(java.time.OffsetDateTime.now());
        refreshTokenRepository.revokeAllForUser(user);
    }

    // --- helpers ---------------------------------------------------------------------------------

    private User loadActive(Long id) {
        return userRepository.findActiveByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        Set<Role> roles = new LinkedHashSet<>();
        for (RoleName name : roleNames) {
            roles.add(roleRepository.findByName(name)
                    .orElseThrow(() -> ApiException.badRequest("Unknown role: " + name)));
        }
        return roles;
    }

    private void assertMayAssign(Set<RoleName> roleNames) {
        boolean requestsPrivileged = roleNames.stream().anyMatch(PRIVILEGED_ROLES::contains);
        if (requestsPrivileged && !currentUserIsSuperAdmin()) {
            throw ApiException.forbidden("Only a Super Admin can assign administrative or HR roles");
        }
    }

    private boolean currentUserIsSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + RoleName.SUPER_ADMIN.name()));
    }
}
