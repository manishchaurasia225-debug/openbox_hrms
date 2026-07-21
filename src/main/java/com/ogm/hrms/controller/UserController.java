package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.user.CreateUserRequest;
import com.ogm.hrms.dto.user.UpdateUserRolesRequest;
import com.ogm.hrms.dto.user.UserResponse;
import com.ogm.hrms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative user-management API. Every endpoint is authorized by a specific RBAC permission
 * (matrix codes AUTH:*, AUTHZ:EDIT); role-assignment business rules are enforced in the service.
 */
@Tag(name = "User Management", description = "Administrative management of user accounts, roles, and status.")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a user",
            description = "Creates a new user account. Requires the AUTH:CREATE permission.")
    @PostMapping
    @PreAuthorize("hasAuthority('AUTH:CREATE')")
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request, HttpServletRequest http) {
        return ApiResponse.success(userService.createUser(request), "User created", http.getRequestURI());
    }

    @Operation(summary = "List users",
            description = "Returns a paginated list of user accounts. Administrative — requires AUTH:CREATE or AUTH:ADMIN. "
                    + "A standard user manages their own account via the /account endpoints, not here.")
    @GetMapping
    @PreAuthorize("hasAuthority('AUTH:CREATE') or hasAuthority('AUTH:ADMIN')")
    public ApiResponse<PageResponse<UserResponse>> list(@PageableDefault(size = 20) Pageable pageable,
                                                        HttpServletRequest http) {
        return ApiResponse.success(userService.listUsers(pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get a user",
            description = "Returns a single user account by its identifier. Administrative — requires AUTH:CREATE or AUTH:ADMIN.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTH:CREATE') or hasAuthority('AUTH:ADMIN')")
    public ApiResponse<UserResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(userService.getUser(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update user roles",
            description = "Replaces the set of roles assigned to a user; assignment rules are enforced in the service. Requires the AUTHZ:EDIT permission.")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('AUTHZ:EDIT')")
    public ApiResponse<UserResponse> updateRoles(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateUserRolesRequest request,
                                                 HttpServletRequest http) {
        return ApiResponse.success(userService.updateRoles(id, request.roles()), "Roles updated", http.getRequestURI());
    }

    @Operation(summary = "Enable or disable a user",
            description = "Toggles a user account's enabled status via the 'enabled' query parameter. "
                    + "Administrative — requires AUTH:CREATE or AUTH:ADMIN.")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('AUTH:CREATE') or hasAuthority('AUTH:ADMIN')")
    public ApiResponse<UserResponse> setStatus(@PathVariable Long id, @RequestParam boolean enabled,
                                               HttpServletRequest http) {
        return ApiResponse.success(userService.setEnabled(id, enabled),
                enabled ? "User enabled" : "User disabled", http.getRequestURI());
    }

    @Operation(summary = "Delete a user",
            description = "Deletes a user account by its identifier. Requires the AUTH:DELETE permission.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTH:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        userService.deleteUser(id);
        return ApiResponse.success(null, "User deleted", http.getRequestURI());
    }
}
