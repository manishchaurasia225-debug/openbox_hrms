package com.ogm.hrms.lifecycle;

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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Employee Lifecycle against real PostgreSQL: onboarding checklist completes
 * the case when all tasks are done, and RBAC (only EMPLOYEE:CREATE can initiate).
 */
@AutoConfigureMockMvc
@Transactional
class LifecycleIT extends AbstractPostgresIntegrationTest {

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
        seedUser("life.hr@ogm.test", RoleName.HR_MANAGER);   // EMPLOYEE:CREATE/EDIT/VIEW
        seedUser("life.emp@ogm.test", RoleName.EMPLOYEE);    // EMPLOYEE:VIEW/EDIT, no CREATE
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-LIFE-1");
        employee.setFullName("Lifecycle Person");
        employeeId = employees.saveAndFlush(employee).getId();
    }

    @Test
    void onboardingChecklistCompletesWhenAllTasksDone() throws Exception {
        String token = login("life.hr@ogm.test");

        MvcResult initiated = mockMvc.perform(post("/api/v1/lifecycle").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"ONBOARDING\",\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INITIATED"))
                .andExpect(jsonPath("$.data.tasks.length()").value(5))
                .andReturn();
        String body = initiated.getResponse().getContentAsString();
        int caseId = JsonPath.read(body, "$.data.id");
        List<Integer> taskIds = JsonPath.read(body, "$.data.tasks[*].id");

        for (int i = 0; i < taskIds.size(); i++) {
            var result = mockMvc.perform(post("/api/v1/lifecycle/" + caseId + "/tasks/" + taskIds.get(i) + "/complete")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            if (i < taskIds.size() - 1) {
                result.andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
            } else {
                result.andExpect(jsonPath("$.data.status").value("COMPLETED"))
                        .andExpect(jsonPath("$.data.completedDate").isNotEmpty());
            }
        }
    }

    @Test
    void plainEmployeeCannotInitiate() throws Exception {
        String token = login("life.emp@ogm.test");
        mockMvc.perform(post("/api/v1/lifecycle").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"OFFBOARDING\",\"employeeId\":" + employeeId + "}"))
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
