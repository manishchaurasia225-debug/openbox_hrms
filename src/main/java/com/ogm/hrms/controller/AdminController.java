package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.admin.LoginHistoryResponse;
import com.ogm.hrms.dto.admin.RolePermissionsResponse;
import com.ogm.hrms.dto.admin.SystemInfoResponse;
import com.ogm.hrms.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * System Administration console API. Read-only administrative surfaces; each endpoint is gated by the
 * most specific relevant authority (system config, authorization model, audit).
 */
@Tag(name = "System Administration", description = "Read-only administrative console: system info, role catalogue, and login history.")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Get system information",
            description = "Returns runtime and deployment system information for the platform. Requires the SETTINGS:ADMIN permission.")
    @GetMapping("/system-info")
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<SystemInfoResponse> systemInfo(HttpServletRequest http) {
        return ApiResponse.success(adminService.systemInfo(), "OK", http.getRequestURI());
    }

    @Operation(summary = "List roles and permissions",
            description = "Returns the catalogue of roles with their associated permissions. Requires the AUTHZ:VIEW permission.")
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('AUTHZ:VIEW')")
    public ApiResponse<List<RolePermissionsResponse>> roles(HttpServletRequest http) {
        return ApiResponse.success(adminService.rolesCatalogue(), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get login history",
            description = "Returns a paginated login-history log, optionally filtered by email. Requires the AUDIT:VIEW permission.")
    @GetMapping("/login-history")
    @PreAuthorize("hasAuthority('AUDIT:VIEW')")
    public ApiResponse<PageResponse<LoginHistoryResponse>> loginHistory(
            @RequestParam(required = false) String email,
            @PageableDefault(size = 50) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(adminService.loginHistory(email, pageable), "OK", http.getRequestURI());
    }
}
