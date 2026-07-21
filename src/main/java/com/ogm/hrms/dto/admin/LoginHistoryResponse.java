package com.ogm.hrms.dto.admin;

import java.time.OffsetDateTime;

/** API view of a login attempt record. */
public record LoginHistoryResponse(
        Long id,
        String email,
        boolean successful,
        String ipAddress,
        String userAgent,
        String failureReason,
        OffsetDateTime occurredAt
) {
}
