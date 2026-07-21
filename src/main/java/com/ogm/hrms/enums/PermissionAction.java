package com.ogm.hrms.enums;

/**
 * The permission actions defined by {@code permissions-matrix.md} §3. Combined with a
 * {@link PermissionModule} they form a permission code (e.g. {@code EMPLOYEE:VIEW}).
 */
public enum PermissionAction {
    VIEW,
    CREATE,
    EDIT,
    DELETE,
    APPROVE,
    EXPORT,
    ADMIN
}
