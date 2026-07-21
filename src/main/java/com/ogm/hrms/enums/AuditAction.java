package com.ogm.hrms.enums;

/**
 * The kind of audited event. Covers the categories called out in CLAUDE.md (authentication, entity
 * changes, permission/config changes, downloads). HTTP-captured events map method→action
 * (POST→CREATE, PUT/PATCH→UPDATE, DELETE→DELETE); auth and file events are recorded explicitly.
 */
public enum AuditAction {
    LOGIN,
    LOGIN_FAILED,
    LOGOUT,
    CREATE,
    UPDATE,
    DELETE,
    DOWNLOAD,
    EXPORT,
    PERMISSION_CHANGE,
    CONFIG_CHANGE,
    OTHER
}
