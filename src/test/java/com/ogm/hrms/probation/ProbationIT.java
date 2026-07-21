package com.ogm.hrms.probation;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.RoleRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
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
 * Integration tests for Confirmation & Probation against real PostgreSQL: start → extend → confirm,
 * duplicate-active prevention, and RBAC (confirm requires EMPLOYEE:APPROVE).
 */
@AutoConfigureMockMvc
@Transactional
class ProbationIT extends AbstractPostgresIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    private Long employeeId;

    @BeforeEach
    void seed() {
        seedUser("prob.hrm@ogm.test", RoleName.HR_MANAGER);     // EMPLOYEE:EDIT + APPROVE + VIEW
        seedUser("prob.hrx@ogm.test", RoleName.HR_EXECUTIVE);   // EMPLOYEE:EDIT + VIEW, no APPROVE
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-PROB-1");
        employee.setFullName("Probation Person");
        employeeId = employees.saveAndFlush(employee).getId();
    }

    @Test
    void startExtendConfirmFlow() throws Exception {
        String token = login("prob.hrm@ogm.test");

        MvcResult started = mockMvc.perform(post("/api/v1/probation").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + ",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROBATION"))
                .andReturn();
        int id = JsonPath.read(started.getResponse().getContentAsString(), "$.data.id");

        // Duplicate active probation -> 409
        mockMvc.perform(post("/api/v1/probation").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + ",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/probation/" + id + "/extend").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newEndDate\":\"2026-09-30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EXTENDED"))
                .andExpect(jsonPath("$.data.endDate").value("2026-09-30"));

        mockMvc.perform(post("/api/v1/probation/" + id + "/confirm").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.confirmationDate").isNotEmpty());
    }

    @Test
    void hrExecutiveCannotConfirm() throws Exception {
        String hrxToken = login("prob.hrx@ogm.test");
        MvcResult started = mockMvc.perform(post("/api/v1/probation").header("Authorization", "Bearer " + hrxToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + ",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isOk())
                .andReturn();
        int id = JsonPath.read(started.getResponse().getContentAsString(), "$.data.id");

        // HR Executive lacks EMPLOYEE:APPROVE -> cannot confirm
        mockMvc.perform(post("/api/v1/probation/" + id + "/confirm").header("Authorization", "Bearer " + hrxToken))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private void seedUser(String email, RoleName roleName) {
        Role role = roles.findByName(roleName).orElseThrow();
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setFullName("Test " + roleName.name());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.addRole(role);
        users.saveAndFlush(user);
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
