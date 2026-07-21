package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.audit.AuditLogResponse;
import com.ogm.hrms.enums.AuditAction;
import com.ogm.hrms.enums.AuditOutcome;
import com.ogm.hrms.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/** Read-only audit trail API, authorized by the {@code AUDIT} RBAC permissions (append-only ledger). */
@Tag(name = "Audit Logs", description = "Read-only, append-only audit trail of system and user actions.")
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "Search audit logs",
            description = "Returns a paginated audit trail filterable by action, outcome, module, actor email, and time range. Requires the AUDIT:VIEW permission.")
    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT:VIEW')")
    public ApiResponse<PageResponse<AuditLogResponse>> list(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) AuditOutcome outcome,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @PageableDefault(size = 50) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(
                auditService.search(action, outcome, module, actorEmail, from, to, pageable),
                "OK", http.getRequestURI());
    }

    @Operation(summary = "Get an audit log entry",
            description = "Returns a single audit log entry by its identifier. Requires the AUDIT:VIEW permission.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT:VIEW')")
    public ApiResponse<AuditLogResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(auditService.get(id), "OK", http.getRequestURI());
    }
}
