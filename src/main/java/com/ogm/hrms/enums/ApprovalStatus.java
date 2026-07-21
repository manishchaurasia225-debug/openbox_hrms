package com.ogm.hrms.enums;

/** Generic approval state for items that may require authorization (e.g. WFH, corrections). */
public enum ApprovalStatus {
    NOT_REQUIRED,
    PENDING,
    APPROVED,
    REJECTED
}
