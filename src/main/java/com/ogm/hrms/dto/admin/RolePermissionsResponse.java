package com.ogm.hrms.dto.admin;

import com.ogm.hrms.enums.RoleName;

import java.util.List;

/** A role and the permission codes granted to it, for the admin roles/permissions catalogue. */
public record RolePermissionsResponse(RoleName role, int permissionCount, List<String> permissions) {
}
