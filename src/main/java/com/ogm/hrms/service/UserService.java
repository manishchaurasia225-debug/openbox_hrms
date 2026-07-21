package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.user.CreateUserRequest;
import com.ogm.hrms.dto.user.UserResponse;
import com.ogm.hrms.enums.RoleName;
import org.springframework.data.domain.Pageable;

import java.util.Set;

/**
 * Administrative user-account management: creation, listing, role assignment, enable/disable, and
 * soft deletion. Endpoint-level authorization is enforced by {@code @PreAuthorize}; role-assignment
 * business rules (only a Super Admin may grant administrative/HR roles) are enforced here.
 */
public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    PageResponse<UserResponse> listUsers(Pageable pageable);

    UserResponse getUser(Long id);

    UserResponse updateRoles(Long id, Set<RoleName> roles);

    UserResponse setEnabled(Long id, boolean enabled);

    void deleteUser(Long id);
}
