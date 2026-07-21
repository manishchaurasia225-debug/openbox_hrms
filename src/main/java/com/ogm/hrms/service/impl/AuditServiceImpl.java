package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.audit.AuditLogResponse;
import com.ogm.hrms.entity.AuditLog;
import com.ogm.hrms.enums.AuditAction;
import com.ogm.hrms.enums.AuditOutcome;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.AuditLogRepository;
import com.ogm.hrms.service.AuditService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Default audit service. Producer methods run in a new transaction ({@code REQUIRES_NEW}) and swallow
 * failures, so an audit write neither rolls back nor breaks the business operation it describes — and
 * a failed-login audit survives even when the surrounding request transaction rolls back.
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);
    private static final int MAX_DESCRIPTION = 500;

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAuthEvent(AuditAction action, AuditOutcome outcome, String actorEmail, Long actorId,
                                String description, String ipAddress, String userAgent) {
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setOutcome(outcome);
        entry.setModule("AUTH");
        entry.setActorEmail(actorEmail);
        entry.setActorId(actorId);
        entry.setDescription(truncate(description));
        entry.setIpAddress(ipAddress);
        entry.setUserAgent(truncate(userAgent, 300));
        persistQuietly(entry);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordHttpRequest(String httpMethod, String path, int statusCode, String actorEmail, Long actorId,
                                  String ipAddress, String userAgent) {
        AuditLog entry = new AuditLog();
        entry.setAction(deriveAction(httpMethod, path));
        entry.setOutcome(statusCode < 400 ? AuditOutcome.SUCCESS : AuditOutcome.FAILURE);
        entry.setModule(resourceSegment(path));
        entry.setEntityType(resourceSegment(path));
        entry.setEntityId(trailingId(path));
        entry.setActorEmail(actorEmail);
        entry.setActorId(actorId);
        entry.setDescription(truncate(httpMethod + " " + path + " -> " + statusCode));
        entry.setHttpMethod(httpMethod);
        entry.setPath(truncate(path, 300));
        entry.setStatusCode(statusCode);
        entry.setIpAddress(ipAddress);
        entry.setUserAgent(truncate(userAgent, 300));
        persistQuietly(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(AuditAction action, AuditOutcome outcome, String module,
                                                 String actorEmail, OffsetDateTime from, OffsetDateTime to,
                                                 Pageable pageable) {
        String moduleFilter = blankToNull(module);
        String actorFilter = blankToNull(actorEmail);
        // Build predicates only for supplied filters — avoids untyped-null parameters entirely.
        Specification<com.ogm.hrms.entity.AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (outcome != null) {
                predicates.add(cb.equal(root.get("outcome"), outcome));
            }
            if (moduleFilter != null) {
                predicates.add(cb.equal(cb.lower(root.get("module")), moduleFilter.toLowerCase()));
            }
            if (actorFilter != null) {
                predicates.add(cb.equal(cb.lower(root.get("actorEmail")), actorFilter.toLowerCase()));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        PageRequest sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(auditLogRepository.findAll(spec, sorted), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse get(Long id) {
        return toResponse(auditLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id)));
    }

    // --- derivation ------------------------------------------------------------------------------

    private AuditAction deriveAction(String httpMethod, String path) {
        String p = path != null ? path.toLowerCase(java.util.Locale.ROOT) : "";
        if (p.contains("/settings") || p.contains("/company")) {
            return AuditAction.CONFIG_CHANGE;
        }
        if (p.contains("/roles") || p.contains("/permissions")) {
            return AuditAction.PERMISSION_CHANGE;
        }
        return switch (httpMethod != null ? httpMethod.toUpperCase(java.util.Locale.ROOT) : "") {
            case "POST" -> AuditAction.CREATE;
            case "PUT", "PATCH" -> AuditAction.UPDATE;
            case "DELETE" -> AuditAction.DELETE;
            case "GET" -> AuditAction.DOWNLOAD;
            default -> AuditAction.OTHER;
        };
    }

    /** The resource segment after {@code /api/v1/} (e.g. {@code employees}). */
    private String resourceSegment(String path) {
        if (path == null) {
            return null;
        }
        String marker = "/api/v1/";
        int idx = path.indexOf(marker);
        if (idx < 0) {
            return null;
        }
        String rest = path.substring(idx + marker.length());
        int slash = rest.indexOf('/');
        String segment = slash >= 0 ? rest.substring(0, slash) : rest;
        int query = segment.indexOf('?');
        if (query >= 0) {
            segment = segment.substring(0, query);
        }
        return segment.isBlank() ? null : segment;
    }

    /** The right-most numeric path segment as an id, if any (e.g. {@code /employees/5/x} → 5). */
    private Long trailingId(String path) {
        if (path == null) {
            return null;
        }
        String[] segments = path.split("\\?")[0].split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            if (segments[i].matches("\\d+")) {
                try {
                    return Long.valueOf(segments[i]);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    // --- helpers ---------------------------------------------------------------------------------

    private void persistQuietly(AuditLog entry) {
        try {
            auditLogRepository.save(entry);
        } catch (RuntimeException ex) {
            log.warn("Failed to write audit log ({} {}): {}", entry.getAction(), entry.getPath(), ex.getMessage());
        }
    }

    private String blankToNull(String value) {
        return value != null && !value.isBlank() ? value : null;
    }

    private String truncate(String value) {
        return truncate(value, MAX_DESCRIPTION);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(a.getId(), a.getAction(), a.getOutcome(), a.getModule(), a.getEntityType(),
                a.getEntityId(), a.getActorEmail(), a.getActorId(), a.getDescription(), a.getHttpMethod(),
                a.getPath(), a.getStatusCode(), a.getIpAddress(), a.getCreatedAt());
    }
}
