package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.AuditAction;
import com.ogm.hrms.enums.AuditOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An append-only audit trail entry. Written by the {@code AuditService} producer (HTTP interceptor for
 * mutations/downloads, explicit calls for auth events) and never updated or deleted through the API.
 * The actor is denormalised (email + id, no FK) so the trail survives even if the user record changes.
 * {@code createdAt}/{@code createdBy} (from {@link BaseEntity}) provide the timestamp and JPA-audited
 * principal.
 */
@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_logs_action", columnList = "action"),
                @Index(name = "idx_audit_logs_actor", columnList = "actor_email"),
                @Index(name = "idx_audit_logs_module", columnList = "module"),
                @Index(name = "idx_audit_logs_created", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class AuditLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 10)
    private AuditOutcome outcome = AuditOutcome.SUCCESS;

    /** The functional area / resource affected (e.g. {@code employees}, {@code AUTH}). */
    @Column(name = "module", length = 60)
    private String module;

    @Column(name = "entity_type", length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "actor_email", length = 190)
    private String actorEmail;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "path", length = 300)
    private String path;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 300)
    private String userAgent;
}
