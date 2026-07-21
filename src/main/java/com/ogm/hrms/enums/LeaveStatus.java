package com.ogm.hrms.enums;

/** Lifecycle of a leave request (two-level approval: manager then HR). */
public enum LeaveStatus {
    PENDING,
    MANAGER_APPROVED,
    APPROVED,
    REJECTED,
    CANCELLED
}
