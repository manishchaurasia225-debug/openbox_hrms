package com.ogm.hrms.service;

import com.ogm.hrms.dto.dashboard.EmployeeDashboardResponse;
import com.ogm.hrms.dto.dashboard.HrDashboardResponse;
import com.ogm.hrms.security.AuthenticatedUser;

/** Analytics dashboards (RBAC module {@code DASHBOARD}). */
public interface DashboardService {

    HrDashboardResponse hrDashboard();

    EmployeeDashboardResponse myDashboard(AuthenticatedUser principal);
}
