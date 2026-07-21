package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.admin.LoginHistoryResponse;
import com.ogm.hrms.dto.admin.RolePermissionsResponse;
import com.ogm.hrms.dto.admin.SystemInfoResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * System Administration (Module 24): read-only administrative surfaces — system health/info, the
 * role→permission catalogue, and login history — composed from existing services and the RBAC model.
 * Settings and user administration are covered by their own modules; this module does not duplicate them.
 */
public interface AdminService {

    SystemInfoResponse systemInfo();

    List<RolePermissionsResponse> rolesCatalogue();

    /** Login attempts, newest first; optionally filtered by email. */
    PageResponse<LoginHistoryResponse> loginHistory(String email, Pageable pageable);
}
