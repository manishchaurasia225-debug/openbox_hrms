package com.ogm.hrms.security;

import com.ogm.hrms.repository.EmployeeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Read-only view over the current security context for data-scope enforcement in services.
 *
 * <p>Permission grants say <em>which</em> {@code MODULE:ACTION}s a caller may invoke; this helper
 * answers <em>whose</em> data they may touch (self vs. all). The permissions matrix (decision D-005)
 * defines the self/team/own scope suffixes as query-time concerns rather than distinct grants, so
 * that enforcement lives here — not in {@code @PreAuthorize}.</p>
 */
@Component
public class CurrentAccess {

    private final EmployeeRepository employeeRepository;

    public CurrentAccess(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /** @return the authenticated user's id, or {@code null} if the request is unauthenticated. */
    public Long userId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user.id();
        }
        return null;
    }

    /** @return true if the caller holds ANY of the given {@code MODULE:ACTION} authority codes. */
    public boolean hasAnyAuthority(String... codes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        Set<String> held = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        for (String code : codes) {
            if (held.contains(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the id of the employee record linked to the current user, or {@code null} when the
     * caller has no linked employee profile (e.g. an administrator account).
     */
    public Long employeeId() {
        Long uid = userId();
        if (uid == null) {
            return null;
        }
        return employeeRepository.findByUser_IdAndDeletedFalse(uid).map(e -> e.getId()).orElse(null);
    }
}
