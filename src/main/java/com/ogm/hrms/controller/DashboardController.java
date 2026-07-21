package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.dashboard.EmployeeDashboardResponse;
import com.ogm.hrms.dto.dashboard.HrDashboardResponse;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Analytics dashboards. The HR dashboard aggregates company-wide widgets ({@code DASHBOARD:VIEW});
 * the employee dashboard is the caller's own self-service summary.
 */
@Tag(name = "Dashboards", description = "Aggregated HR analytics and per-employee self-service dashboard summaries.")
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get HR dashboard", description = "Returns company-wide aggregated HR dashboard widgets. Requires DASHBOARD:VIEW.")
    @GetMapping("/hr")
    @PreAuthorize("hasAuthority('DASHBOARD:VIEW')")
    public ApiResponse<HrDashboardResponse> hr(HttpServletRequest http) {
        return ApiResponse.success(dashboardService.hrDashboard(), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get my dashboard", description = "Returns the authenticated user's own self-service dashboard summary. Requires DASHBOARD:VIEW.")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('DASHBOARD:VIEW')")
    public ApiResponse<EmployeeDashboardResponse> me(@AuthenticationPrincipal AuthenticatedUser principal,
                                                     HttpServletRequest http) {
        return ApiResponse.success(dashboardService.myDashboard(principal), "OK", http.getRequestURI());
    }
}
