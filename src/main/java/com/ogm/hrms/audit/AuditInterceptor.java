package com.ogm.hrms.audit;

import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Records an audit entry for state-changing HTTP requests (POST/PUT/PATCH/DELETE) and file downloads
 * under {@code /api/v1/**}, capturing the acting user, path, and outcome without touching each
 * service. Authentication endpoints are excluded — those are audited explicitly with richer context
 * (failure reasons, actor on failed login) by the auth service.
 */
public class AuditInterceptor implements HandlerInterceptor {

    private static final Set<String> MUTATING = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditService auditService;

    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/auth/")) {
            return;  // auth events are recorded explicitly by AuthService
        }
        if (!shouldAudit(request.getMethod(), path)) {
            return;
        }
        AuthenticatedUser actor = currentActor();
        auditService.recordHttpRequest(request.getMethod(), path, response.getStatus(),
                actor != null ? actor.email() : null, actor != null ? actor.id() : null,
                clientIp(request), request.getHeader("User-Agent"));
    }

    private boolean shouldAudit(String method, String path) {
        if (MUTATING.contains(method.toUpperCase(java.util.Locale.ROOT))) {
            return true;
        }
        // Audit reads only when they represent a download/export of data.
        String p = path.toLowerCase(java.util.Locale.ROOT);
        return "GET".equalsIgnoreCase(method)
                && (p.contains("/reports/") || p.contains("/download") || p.contains("/export"));
    }

    private AuthenticatedUser currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
