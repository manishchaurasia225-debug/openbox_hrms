package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.audit.AuditLogResponse;
import com.ogm.hrms.enums.AuditAction;
import com.ogm.hrms.enums.AuditOutcome;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * Audit trail (Module 23). A producer API for recording events (append-only) plus a query API. Writes
 * are best-effort and isolated from the caller's transaction, so recording an audit event can never
 * fail or roll back a business operation.
 */
public interface AuditService {

    /** Records an authentication event (login success/failure, logout). */
    void recordAuthEvent(AuditAction action, AuditOutcome outcome, String actorEmail, Long actorId,
                         String description, String ipAddress, String userAgent);

    /** Records a mutating/download HTTP request; derives the action, module, and entity from the path. */
    void recordHttpRequest(String httpMethod, String path, int statusCode, String actorEmail, Long actorId,
                           String ipAddress, String userAgent);

    PageResponse<AuditLogResponse> search(AuditAction action, AuditOutcome outcome, String module,
                                          String actorEmail, OffsetDateTime from, OffsetDateTime to,
                                          Pageable pageable);

    AuditLogResponse get(Long id);
}
