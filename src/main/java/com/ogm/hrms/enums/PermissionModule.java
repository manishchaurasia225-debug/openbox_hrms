package com.ogm.hrms.enums;

/**
 * The functional modules that authorization is scoped to, per {@code permissions-matrix.md}.
 *
 * <p>Shift Management is intentionally absent — it is a removed feature (decision D-006).
 * Company/Branch modules are retained as configuration surfaces but operate single-company for now
 * (decision D-004); cross-tenant semantics are inert.</p>
 */
public enum PermissionModule {
    AUTH,
    AUTHZ,
    SETTINGS,
    AUDIT,
    COMPANY,
    BRANCH,
    DEPARTMENT,
    DESIGNATION,
    EMPLOYEE,
    DOCUMENT,
    ATTENDANCE,
    LEAVE,
    HOLIDAY,
    PAYROLL,
    EXPENSE,
    RECRUITMENT,
    PERFORMANCE,
    ASSET,
    ANNOUNCEMENT,
    NOTIFICATION,
    EMAIL,
    WHATSAPP,
    AI,
    REPORT,
    DASHBOARD
}
