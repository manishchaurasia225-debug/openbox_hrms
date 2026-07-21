package com.ogm.hrms.repository;

import com.ogm.hrms.entity.Permission;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the RBAC/user persistence layer against real PostgreSQL (Testcontainers).
 * Verifies the seeded roles, the applied permission matrix (D-005), the fetch-join loader, JPA
 * auditing, and soft-delete exclusion.
 */
@Transactional
class RbacPersistenceIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserRepository users;
    @Autowired
    private RoleRepository roles;

    @Test
    void seedsAllRolesAndAppliesThePermissionMatrix() {
        assertThat(roles.count()).isEqualTo(RoleName.values().length);

        Set<String> superAdmin = codesOf(RoleName.SUPER_ADMIN);
        assertThat(superAdmin).contains("EMPLOYEE:VIEW", "AUTHZ:ADMIN", "PAYROLL:ADMIN");
        // Super Admin is deliberately restricted from operational payroll actions.
        assertThat(superAdmin).doesNotContain("PAYROLL:CREATE", "PAYROLL:APPROVE");

        Set<String> hrManager = codesOf(RoleName.HR_MANAGER);
        assertThat(hrManager).contains("EMPLOYEE:CREATE", "LEAVE:APPROVE", "PAYROLL:APPROVE");

        Set<String> employee = codesOf(RoleName.EMPLOYEE);
        assertThat(employee).contains("EMPLOYEE:VIEW", "LEAVE:CREATE");
        assertThat(employee).doesNotContain("EMPLOYEE:DELETE", "PAYROLL:APPROVE", "AUTHZ:VIEW");

        // Shift Management is a removed feature — no such permissions exist at all.
        assertThat(superAdmin).noneMatch(code -> code.startsWith("SHIFT:"));
    }

    @Test
    void persistsAndLoadsActiveUserWithRolesAndPermissions() {
        Role superAdmin = roles.findByName(RoleName.SUPER_ADMIN).orElseThrow();

        User user = new User();
        user.setEmail("admin@ogm.test");
        user.setPasswordHash("$2a$10$notarealhashnotarealhashnotarealhashnotarealhashxx");
        user.setFullName("Admin Person");
        user.addRole(superAdmin);
        users.saveAndFlush(user);

        Optional<User> loaded = users.findActiveByEmailWithRolesAndPermissions("ADMIN@OGM.TEST");

        assertThat(loaded).isPresent();
        User found = loaded.get();
        assertThat(found.getCreatedAt()).isNotNull();          // JPA auditing populated
        assertThat(found.getCreatedBy()).isEqualTo("system");  // no auth context -> system
        assertThat(found.getRoles()).extracting(Role::getName).containsExactly(RoleName.SUPER_ADMIN);
        assertThat(found.getRoles().iterator().next().getPermissions())
                .extracting(Permission::getCode).contains("EMPLOYEE:VIEW", "PAYROLL:VIEW");
    }

    @Test
    void softDeletedUserIsExcludedFromActiveLookup() {
        User user = new User();
        user.setEmail("gone@ogm.test");
        user.setPasswordHash("$2a$10$notarealhashnotarealhashnotarealhashnotarealhashxx");
        user.setFullName("Former Employee");
        user.setDeleted(true);
        users.saveAndFlush(user);

        assertThat(users.findActiveByEmailWithRolesAndPermissions("gone@ogm.test")).isEmpty();
        assertThat(users.findByEmailIgnoreCase("gone@ogm.test")).isPresent();
    }

    private Set<String> codesOf(RoleName roleName) {
        return roles.findByName(roleName).orElseThrow().getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }
}
