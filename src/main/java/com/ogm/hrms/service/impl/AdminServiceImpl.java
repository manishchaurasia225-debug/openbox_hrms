package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.admin.LoginHistoryResponse;
import com.ogm.hrms.dto.admin.RolePermissionsResponse;
import com.ogm.hrms.dto.admin.SystemInfoResponse;
import com.ogm.hrms.entity.LoginHistory;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.LoginHistoryRepository;
import com.ogm.hrms.repository.PermissionRepository;
import com.ogm.hrms.repository.RoleRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.security.RolePermissionMatrix;
import com.ogm.hrms.service.AdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Default admin service. Read-only: it summarises system state, projects the canonical
 * {@link RolePermissionMatrix} into a catalogue, and exposes login history. The start time is captured
 * at bean construction (context refresh, i.e. ~startup) to report uptime without extra infrastructure.
 */
@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final Environment environment;
    private final String applicationName;
    private final String version;
    private final OffsetDateTime startedAt = OffsetDateTime.now();

    public AdminServiceImpl(UserRepository userRepository, EmployeeRepository employeeRepository,
                            DepartmentRepository departmentRepository, PermissionRepository permissionRepository,
                            RoleRepository roleRepository, LoginHistoryRepository loginHistoryRepository,
                            Environment environment,
                            @Value("${spring.application.name:hrms}") String applicationName,
                            @Value("${hrms.app.version:0.0.1-SNAPSHOT}") String version) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.environment = environment;
        this.applicationName = applicationName;
        this.version = version;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemInfoResponse systemInfo() {
        String databaseStatus;
        long users;
        long employees;
        long departments;
        long roles;
        long permissions;
        try {
            users = userRepository.count();
            employees = employeeRepository.countByDeletedFalse();
            departments = departmentRepository.countByDeletedFalse();
            roles = roleRepository.count();
            permissions = permissionRepository.count();
            databaseStatus = "UP";
        } catch (RuntimeException ex) {
            return new SystemInfoResponse(applicationName, version, List.of(environment.getActiveProfiles()),
                    startedAt, uptimeSeconds(), "DOWN", new SystemInfoResponse.Counts(0, 0, 0, 0, 0));
        }
        return new SystemInfoResponse(applicationName, version, List.of(environment.getActiveProfiles()),
                startedAt, uptimeSeconds(), databaseStatus,
                new SystemInfoResponse.Counts(users, employees, departments, roles, permissions));
    }

    @Override
    public List<RolePermissionsResponse> rolesCatalogue() {
        Map<com.ogm.hrms.enums.RoleName, Set<String>> grants =
                new TreeMap<>(RolePermissionMatrix.grants());
        return grants.entrySet().stream()
                .map(e -> new RolePermissionsResponse(e.getKey(), e.getValue().size(),
                        new TreeSet<>(e.getValue()).stream().toList()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoginHistoryResponse> loginHistory(String email, Pageable pageable) {
        var page = (email != null && !email.isBlank())
                ? loginHistoryRepository.findByEmailIgnoreCaseOrderByCreatedAtDesc(email.trim(), pageable)
                : loginHistoryRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.of(page, this::toResponse);
    }

    private long uptimeSeconds() {
        return Duration.between(startedAt, OffsetDateTime.now()).getSeconds();
    }

    private LoginHistoryResponse toResponse(LoginHistory h) {
        return new LoginHistoryResponse(h.getId(), h.getEmail(), h.isSuccessful(), h.getIpAddress(),
                h.getUserAgent(), h.getFailureReason(), h.getCreatedAt());
    }
}
