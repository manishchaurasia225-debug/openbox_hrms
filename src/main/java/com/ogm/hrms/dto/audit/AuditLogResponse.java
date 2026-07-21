package com.ogm.hrms.dto.audit;

import com.ogm.hrms.enums.AuditAction;
import com.ogm.hrms.enums.AuditOutcome;

import java.time.OffsetDateTime;

/** API view of an audit trail entry. */
public record AuditLogResponse(
        Long id,
        AuditAction action,
        AuditOutcome outcome,
        String module,
        String entityType,
        Long entityId,
        String actorEmail,
        Long actorId,
        String description,
        String httpMethod,
        String path,
        Integer statusCode,
        String ipAddress,
        OffsetDateTime occurredAt
) {
}
