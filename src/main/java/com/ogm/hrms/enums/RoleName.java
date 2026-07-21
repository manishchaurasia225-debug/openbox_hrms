package com.ogm.hrms.enums;

/**
 * The canonical set of internal roles, per {@code permissions-matrix.md} (decision D-005).
 *
 * <p>These are the well-known roles seeded at bootstrap and referenced by the authorization model.
 * Role-to-permission grants are stored as data (dynamic RBAC), not hardcoded here — this enum only
 * fixes the role identity. The external Candidate actor is intentionally excluded from the internal
 * grid (portal-only access).</p>
 */
public enum RoleName {
    SUPER_ADMIN,
    COMPANY_ADMIN,
    HR_MANAGER,
    HR_EXECUTIVE,
    MANAGER,
    TEAM_LEAD,
    EMPLOYEE,
    RECRUITER,
    FINANCE
}
