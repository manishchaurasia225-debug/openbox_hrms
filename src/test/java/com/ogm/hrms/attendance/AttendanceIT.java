package com.ogm.hrms.attendance;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.SystemSetting;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.RoleRepository;
import com.ogm.hrms.repository.SystemSettingRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Attendance (Wi-Fi/IP based) against real PostgreSQL. MockMvc requests
 * originate from 127.0.0.1, so the office-ip-allowlist setting is tuned per test.
 */
@AutoConfigureMockMvc
@Transactional
class AttendanceIT extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Sup3rStr0ng!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository users;
    @Autowired
    private RoleRepository roles;
    @Autowired
    private EmployeeRepository employees;
    @Autowired
    private SystemSettingRepository settings;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void officeCheckInThenCheckOutAndDuplicateRejected() throws Exception {
        setAllowlist("127.0.0.1");
        createLinkedEmployee("att.office@ogm.test", RoleName.EMPLOYEE);
        String token = login("att.office@ogm.test");

        mockMvc.perform(post("/api/v1/attendance/check-in").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"mode\":\"OFFICE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceType").value("OFFICE"))
                .andExpect(jsonPath("$.data.source").value("WIFI_IP"));

        // Second check-in same day -> 409
        mockMvc.perform(post("/api/v1/attendance/check-in").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"mode\":\"OFFICE\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/attendance/check-out").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clockOut").isNotEmpty());
    }

    @Test
    void wfhCheckInIsPendingThenApprovedByAdmin() throws Exception {
        setAllowlist("10.0.0.1"); // not the caller IP
        createLinkedEmployee("att.wfh@ogm.test", RoleName.EMPLOYEE);
        seedUser("att.admin@ogm.test", RoleName.SUPER_ADMIN);
        String employeeToken = login("att.wfh@ogm.test");

        MvcResult checkedIn = mockMvc.perform(post("/api/v1/attendance/check-in")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"WORK_FROM_HOME\",\"wfhReason\":\"Plumber visit\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceType").value("WORK_FROM_HOME"))
                .andExpect(jsonPath("$.data.approvalStatus").value("PENDING"))
                .andReturn();
        int id = JsonPath.read(checkedIn.getResponse().getContentAsString(), "$.data.id");

        String adminToken = login("att.admin@ogm.test");
        mockMvc.perform(post("/api/v1/attendance/" + id + "/approve").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approvalStatus").value("APPROVED"));
    }

    @Test
    void officeCheckInOutsideNetworkIsRejected() throws Exception {
        setAllowlist("10.0.0.1"); // caller (127.0.0.1) is not on office network
        createLinkedEmployee("att.remote@ogm.test", RoleName.EMPLOYEE);
        String token = login("att.remote@ogm.test");

        mockMvc.perform(post("/api/v1/attendance/check-in").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"mode\":\"OFFICE\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void wfhCheckInWithoutReasonIsRejected() throws Exception {
        createLinkedEmployee("att.noreason@ogm.test", RoleName.EMPLOYEE);
        String token = login("att.noreason@ogm.test");

        mockMvc.perform(post("/api/v1/attendance/check-in").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"mode\":\"WORK_FROM_HOME\"}"))
                .andExpect(status().isBadRequest());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private void setAllowlist(String value) {
        SystemSetting setting = settings.findBySettingKey("attendance.office-ip-allowlist").orElseThrow();
        setting.setSettingValue(value);
        settings.saveAndFlush(setting);
    }

    private void createLinkedEmployee(String email, RoleName roleName) {
        User user = seedUser(email, roleName);
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-" + email.hashCode());
        employee.setFullName("Attendance Person");
        employee.setUser(user);
        employees.saveAndFlush(employee);
    }

    private User seedUser(String email, RoleName roleName) {
        Role role = roles.findByName(roleName).orElseThrow();
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setFullName("Test " + roleName.name());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.addRole(role);
        return users.saveAndFlush(user);
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }
}
