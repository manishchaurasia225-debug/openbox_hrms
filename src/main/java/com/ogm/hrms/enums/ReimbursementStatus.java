package com.ogm.hrms.enums;

/** Lifecycle of a reimbursement claim (manager → finance approval, then payout). */
public enum ReimbursementStatus {
    SUBMITTED,
    MANAGER_APPROVED,
    APPROVED,
    REJECTED,
    PAID,
    CANCELLED
}
