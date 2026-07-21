package com.ogm.hrms.security;

import com.ogm.hrms.enums.PermissionAction;
import com.ogm.hrms.enums.PermissionModule;
import com.ogm.hrms.enums.RoleName;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.ogm.hrms.enums.PermissionAction.ADMIN;
import static com.ogm.hrms.enums.PermissionAction.APPROVE;
import static com.ogm.hrms.enums.PermissionAction.CREATE;
import static com.ogm.hrms.enums.PermissionAction.DELETE;
import static com.ogm.hrms.enums.PermissionAction.EDIT;
import static com.ogm.hrms.enums.PermissionAction.EXPORT;
import static com.ogm.hrms.enums.PermissionAction.VIEW;

/**
 * The authoritative role → permission grant matrix, encoded exactly from
 * {@code docs/01-product/permissions-matrix.md} (decision D-005). Each ✔ cell becomes a
 * {@code MODULE:ACTION} grant; scope suffixes in the matrix (self/team/own) are data-scoping
 * concerns enforced at query time, not permission grants, so they do not change the grant set.
 *
 * <p>Deny-by-default: a role holds only the codes listed here. Shift Management is absent (removed
 * feature, D-006). This class is data only — {@link com.ogm.hrms.config.DataInitializer} applies it
 * to the persisted roles idempotently, so grants remain configurable/editable afterwards.</p>
 */
public final class RolePermissionMatrix {

    private RolePermissionMatrix() {
    }

    /** @return an immutable-in-spirit map of role → set of {@code MODULE:ACTION} permission codes. */
    public static Map<RoleName, Set<String>> grants() {
        Map<RoleName, Set<String>> grants = new EnumMap<>(RoleName.class);
        for (RoleName role : RoleName.values()) {
            grants.put(role, new LinkedHashSet<>());
        }

        // --- Platform & Security ---------------------------------------------------------------
        // AUTH (account/session administration)
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.AUTH, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.AUTH, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.AUTH, VIEW, CREATE, EDIT);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.AUTH, VIEW, CREATE, EDIT);
        add(grants, RoleName.EMPLOYEE, PermissionModule.AUTH, VIEW, EDIT);

        // AUTHZ (role/permission assignment)
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.AUTHZ, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.AUTHZ, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.AUTHZ, VIEW, EDIT);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.AUTHZ, VIEW);

        // SETTINGS
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.SETTINGS, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.SETTINGS, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.SETTINGS, VIEW, CREATE, EDIT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.SETTINGS, VIEW);
        add(grants, RoleName.MANAGER, PermissionModule.SETTINGS, VIEW);
        add(grants, RoleName.EMPLOYEE, PermissionModule.SETTINGS, VIEW, EDIT);
        add(grants, RoleName.RECRUITER, PermissionModule.SETTINGS, VIEW, EDIT);
        add(grants, RoleName.FINANCE, PermissionModule.SETTINGS, VIEW, EDIT);

        // AUDIT (append-only; no create/edit/delete through the app)
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.AUDIT, VIEW, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.AUDIT, VIEW, EXPORT);
        add(grants, RoleName.HR_MANAGER, PermissionModule.AUDIT, VIEW, EXPORT);
        add(grants, RoleName.FINANCE, PermissionModule.AUDIT, VIEW, EXPORT);

        // --- Organization ----------------------------------------------------------------------
        // COMPANY
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.COMPANY, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.COMPANY, VIEW, EDIT, EXPORT, ADMIN);
        addView(grants, PermissionModule.COMPANY, RoleName.HR_MANAGER, RoleName.HR_EXECUTIVE, RoleName.MANAGER,
                RoleName.TEAM_LEAD, RoleName.EMPLOYEE, RoleName.RECRUITER, RoleName.FINANCE);

        // BRANCH
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.BRANCH, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.BRANCH, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.BRANCH, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.BRANCH, VIEW, EDIT);
        addView(grants, PermissionModule.BRANCH, RoleName.MANAGER, RoleName.TEAM_LEAD, RoleName.EMPLOYEE,
                RoleName.RECRUITER, RoleName.FINANCE);

        // DEPARTMENT
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.DEPARTMENT, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.DEPARTMENT, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.DEPARTMENT, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.DEPARTMENT, VIEW, CREATE, EDIT);
        add(grants, RoleName.MANAGER, PermissionModule.DEPARTMENT, VIEW, EXPORT);
        addView(grants, PermissionModule.DEPARTMENT, RoleName.TEAM_LEAD, RoleName.EMPLOYEE, RoleName.RECRUITER,
                RoleName.FINANCE);

        // DESIGNATION
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.DESIGNATION, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.DESIGNATION, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.DESIGNATION, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.DESIGNATION, VIEW, CREATE, EDIT);
        addView(grants, PermissionModule.DESIGNATION, RoleName.MANAGER, RoleName.TEAM_LEAD, RoleName.EMPLOYEE,
                RoleName.RECRUITER, RoleName.FINANCE);

        // --- Core HR ---------------------------------------------------------------------------
        // EMPLOYEE
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.EMPLOYEE, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.EMPLOYEE, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.EMPLOYEE, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.EMPLOYEE, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.MANAGER, PermissionModule.EMPLOYEE, VIEW, APPROVE, EXPORT);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.EMPLOYEE, VIEW);
        add(grants, RoleName.EMPLOYEE, PermissionModule.EMPLOYEE, VIEW, EDIT);
        add(grants, RoleName.RECRUITER, PermissionModule.EMPLOYEE, VIEW);
        add(grants, RoleName.FINANCE, PermissionModule.EMPLOYEE, VIEW, EXPORT);

        // DOCUMENT
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.DOCUMENT, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.DOCUMENT, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.DOCUMENT, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.DOCUMENT, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.MANAGER, PermissionModule.DOCUMENT, VIEW, CREATE, APPROVE);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.DOCUMENT, VIEW);
        add(grants, RoleName.EMPLOYEE, PermissionModule.DOCUMENT, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.RECRUITER, PermissionModule.DOCUMENT, VIEW, CREATE, EDIT);
        add(grants, RoleName.FINANCE, PermissionModule.DOCUMENT, VIEW, CREATE, APPROVE, EXPORT);

        // --- Time & Attendance -----------------------------------------------------------------
        // ATTENDANCE
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.ATTENDANCE, VIEW, CREATE, EDIT, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.ATTENDANCE, VIEW, EDIT, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.ATTENDANCE, VIEW, CREATE, EDIT, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.ATTENDANCE, VIEW, CREATE, EDIT, APPROVE, EXPORT);
        add(grants, RoleName.MANAGER, PermissionModule.ATTENDANCE, VIEW, APPROVE, EXPORT);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.ATTENDANCE, VIEW, APPROVE);
        add(grants, RoleName.EMPLOYEE, PermissionModule.ATTENDANCE, VIEW, CREATE);
        add(grants, RoleName.FINANCE, PermissionModule.ATTENDANCE, VIEW, EXPORT);

        // LEAVE
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.LEAVE, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.LEAVE, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.LEAVE, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.LEAVE, VIEW, CREATE, EDIT, APPROVE, EXPORT);
        add(grants, RoleName.MANAGER, PermissionModule.LEAVE, VIEW, CREATE, APPROVE, EXPORT);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.LEAVE, VIEW, APPROVE);
        add(grants, RoleName.EMPLOYEE, PermissionModule.LEAVE, VIEW, CREATE, EDIT, DELETE);
        add(grants, RoleName.FINANCE, PermissionModule.LEAVE, VIEW, EXPORT);

        // HOLIDAY
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.HOLIDAY, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.HOLIDAY, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.HOLIDAY, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.HOLIDAY, VIEW, CREATE, EDIT);
        addView(grants, PermissionModule.HOLIDAY, RoleName.MANAGER, RoleName.TEAM_LEAD, RoleName.EMPLOYEE,
                RoleName.RECRUITER, RoleName.FINANCE);

        // --- Compensation & Finance ------------------------------------------------------------
        // PAYROLL (Super Admin deliberately restricted from operational actions)
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.PAYROLL, VIEW, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.PAYROLL, VIEW, CREATE, EDIT, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.PAYROLL, VIEW, CREATE, EDIT, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.PAYROLL, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.EMPLOYEE, PermissionModule.PAYROLL, VIEW, EXPORT);
        add(grants, RoleName.FINANCE, PermissionModule.PAYROLL, VIEW, APPROVE, EXPORT);

        // EXPENSE
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.EXPENSE, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.EXPENSE, VIEW, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.EXPENSE, VIEW, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.EXPENSE, VIEW, EXPORT);
        add(grants, RoleName.MANAGER, PermissionModule.EXPENSE, VIEW, APPROVE, EXPORT);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.EXPENSE, VIEW, APPROVE);
        add(grants, RoleName.EMPLOYEE, PermissionModule.EXPENSE, VIEW, CREATE, EDIT, DELETE);
        add(grants, RoleName.RECRUITER, PermissionModule.EXPENSE, VIEW, CREATE, EDIT, DELETE);
        add(grants, RoleName.FINANCE, PermissionModule.EXPENSE, VIEW, APPROVE, EXPORT, ADMIN);

        // --- Talent ----------------------------------------------------------------------------
        // RECRUITMENT
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.RECRUITMENT, VIEW, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.RECRUITMENT, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.RECRUITMENT, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.RECRUITMENT, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.MANAGER, PermissionModule.RECRUITMENT, VIEW, CREATE, EDIT, APPROVE);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.RECRUITMENT, VIEW, APPROVE);
        add(grants, RoleName.RECRUITER, PermissionModule.RECRUITMENT, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT);

        // PERFORMANCE
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.PERFORMANCE, VIEW, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.PERFORMANCE, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.PERFORMANCE, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.PERFORMANCE, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.MANAGER, PermissionModule.PERFORMANCE, VIEW, CREATE, EDIT, APPROVE, EXPORT);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.PERFORMANCE, VIEW, CREATE, EDIT);
        add(grants, RoleName.EMPLOYEE, PermissionModule.PERFORMANCE, VIEW, CREATE, EDIT);

        // --- Operations ------------------------------------------------------------------------
        // ASSET
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.ASSET, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.ASSET, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.ASSET, VIEW, CREATE, EDIT, DELETE, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.ASSET, VIEW, CREATE, EDIT, APPROVE, EXPORT);
        add(grants, RoleName.MANAGER, PermissionModule.ASSET, VIEW, EXPORT);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.ASSET, VIEW);
        add(grants, RoleName.EMPLOYEE, PermissionModule.ASSET, VIEW);
        add(grants, RoleName.FINANCE, PermissionModule.ASSET, VIEW, EXPORT);

        // --- Communication ---------------------------------------------------------------------
        // ANNOUNCEMENT
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.ANNOUNCEMENT, VIEW, CREATE, EDIT, DELETE, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.ANNOUNCEMENT, VIEW, CREATE, EDIT, DELETE, APPROVE, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.ANNOUNCEMENT, VIEW, CREATE, EDIT, DELETE, APPROVE);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.ANNOUNCEMENT, VIEW, CREATE, EDIT);
        add(grants, RoleName.MANAGER, PermissionModule.ANNOUNCEMENT, VIEW, CREATE, EDIT, DELETE);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.ANNOUNCEMENT, VIEW, CREATE, EDIT);
        add(grants, RoleName.EMPLOYEE, PermissionModule.ANNOUNCEMENT, VIEW);
        add(grants, RoleName.RECRUITER, PermissionModule.ANNOUNCEMENT, VIEW, CREATE, EDIT);
        add(grants, RoleName.FINANCE, PermissionModule.ANNOUNCEMENT, VIEW, CREATE, EDIT);

        // NOTIFICATION (personal; ADMIN governs company config/templates)
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.NOTIFICATION, VIEW, CREATE, EDIT, DELETE, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.NOTIFICATION, VIEW, EDIT, DELETE, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.NOTIFICATION, VIEW, EDIT, DELETE, ADMIN);
        addEachAll(grants, PermissionModule.NOTIFICATION, new PermissionAction[]{VIEW, EDIT, DELETE},
                RoleName.HR_EXECUTIVE, RoleName.MANAGER, RoleName.TEAM_LEAD, RoleName.EMPLOYEE,
                RoleName.RECRUITER, RoleName.FINANCE);

        // EMAIL (infrastructure; templates/logs are administrative)
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.EMAIL, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.EMAIL, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.EMAIL, VIEW, CREATE, EDIT);
        add(grants, RoleName.RECRUITER, PermissionModule.EMAIL, VIEW, CREATE, EDIT);

        // WHATSAPP
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.WHATSAPP, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.WHATSAPP, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.WHATSAPP, VIEW, CREATE, EDIT);
        add(grants, RoleName.EMPLOYEE, PermissionModule.WHATSAPP, VIEW, EDIT);
        add(grants, RoleName.RECRUITER, PermissionModule.WHATSAPP, VIEW, CREATE, EDIT);

        // --- Intelligence & Analytics ----------------------------------------------------------
        // AI (APPROVE = authorize AI output for consequential use; AI never self-approves)
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.AI, VIEW, CREATE, EDIT, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.AI, VIEW, CREATE, EDIT, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.AI, VIEW, CREATE, EDIT, APPROVE, EXPORT, ADMIN);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.AI, VIEW, CREATE, EDIT, APPROVE);
        add(grants, RoleName.MANAGER, PermissionModule.AI, VIEW, CREATE, EDIT, APPROVE);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.AI, VIEW, CREATE, EDIT);
        add(grants, RoleName.EMPLOYEE, PermissionModule.AI, VIEW, CREATE);
        add(grants, RoleName.RECRUITER, PermissionModule.AI, VIEW, CREATE, EDIT, APPROVE);
        add(grants, RoleName.FINANCE, PermissionModule.AI, VIEW, CREATE, EDIT, APPROVE);

        // REPORT
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.REPORT, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.REPORT, VIEW, CREATE, EDIT, DELETE, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.REPORT, VIEW, CREATE, EDIT, EXPORT);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.REPORT, VIEW, EXPORT);
        add(grants, RoleName.MANAGER, PermissionModule.REPORT, VIEW, EXPORT);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.REPORT, VIEW);
        add(grants, RoleName.EMPLOYEE, PermissionModule.REPORT, VIEW, EXPORT);
        add(grants, RoleName.RECRUITER, PermissionModule.REPORT, VIEW, EXPORT);
        add(grants, RoleName.FINANCE, PermissionModule.REPORT, VIEW, CREATE, EDIT, EXPORT);

        // DASHBOARD (read/personalize only)
        add(grants, RoleName.SUPER_ADMIN, PermissionModule.DASHBOARD, VIEW, EDIT, EXPORT, ADMIN);
        add(grants, RoleName.COMPANY_ADMIN, PermissionModule.DASHBOARD, VIEW, EDIT, EXPORT, ADMIN);
        add(grants, RoleName.HR_MANAGER, PermissionModule.DASHBOARD, VIEW, EDIT, EXPORT);
        add(grants, RoleName.HR_EXECUTIVE, PermissionModule.DASHBOARD, VIEW, EDIT);
        add(grants, RoleName.MANAGER, PermissionModule.DASHBOARD, VIEW, EDIT);
        add(grants, RoleName.TEAM_LEAD, PermissionModule.DASHBOARD, VIEW, EDIT);
        add(grants, RoleName.EMPLOYEE, PermissionModule.DASHBOARD, VIEW, EDIT);
        add(grants, RoleName.RECRUITER, PermissionModule.DASHBOARD, VIEW, EDIT);
        add(grants, RoleName.FINANCE, PermissionModule.DASHBOARD, VIEW, EDIT, EXPORT);

        return grants;
    }

    private static void add(Map<RoleName, Set<String>> grants, RoleName role, PermissionModule module,
                            PermissionAction... actions) {
        Set<String> codes = grants.get(role);
        for (PermissionAction action : actions) {
            codes.add(module.name() + ":" + action.name());
        }
    }

    /** Grant only VIEW on a module to several roles (a very common row shape). */
    private static void addView(Map<RoleName, Set<String>> grants, PermissionModule module, RoleName... roles) {
        for (RoleName role : roles) {
            add(grants, role, module, VIEW);
        }
    }

    /** Grant the same action set on a module to several roles. */
    private static void addEachAll(Map<RoleName, Set<String>> grants, PermissionModule module,
                                   PermissionAction[] actions, RoleName... roles) {
        for (RoleName role : roles) {
            add(grants, role, module, actions);
        }
    }
}
