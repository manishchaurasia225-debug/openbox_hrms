package com.ogm.hrms.ai;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.Gender;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.RoleRepository;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Enterprise AI Assistant against real PostgreSQL: tool routing for the
 * three capabilities, and — critically — that the assistant cannot invoke a tool the caller lacks
 * permission for (no privilege escalation).
 */
@AutoConfigureMockMvc
@Transactional
class AiAssistantIT extends AbstractPostgresIntegrationTest {

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

    @Test
    void routesEmployeeSearch() throws Exception {
        seedUser("ai.admin@ogm.test", RoleName.SUPER_ADMIN);
        employee("EMP-AI1", "Alice Wonderland");
        String token = login("ai.admin@ogm.test");

        assist(token, "search employees named Alice")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tool").value("employee_search"))
                .andExpect(jsonPath("$.data.answer", containsString("matching 'Alice'")))
                .andExpect(jsonPath("$.data.data[0].name").value("Alice Wonderland"));
    }

    @Test
    void routesAttendanceSearch() throws Exception {
        seedUser("ai.admin2@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("ai.admin2@ogm.test");

        assist(token, "show attendance for 2026-07-01")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tool").value("attendance_search"))
                .andExpect(jsonPath("$.data.answer", containsString("Attendance for 2026-07-01")));
    }

    @Test
    void routesReportGeneration() throws Exception {
        seedUser("ai.admin3@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("ai.admin3@ogm.test");

        assist(token, "generate a salary report as PDF")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tool").value("report_generation"))
                .andExpect(jsonPath("$.data.data.download").value("/api/v1/reports/SALARY?format=PDF"));
    }

    @Test
    void doesNotInvokeToolCallerLacksPermissionFor() throws Exception {
        // TEAM_LEAD has AI:VIEW and REPORT:VIEW, but NOT REPORT:EXPORT — the report tool is unavailable.
        seedUser("ai.lead@ogm.test", RoleName.TEAM_LEAD);
        String token = login("ai.lead@ogm.test");

        assist(token, "generate a salary report as PDF")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tool").value(nullValue()))
                .andExpect(jsonPath("$.data.answer", containsString("couldn't match")));
    }

    // --- helpers ---------------------------------------------------------------------------------

    private org.springframework.test.web.servlet.ResultActions assist(String token, String query) throws Exception {
        return mockMvc.perform(post("/api/v1/ai/assistant")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"" + query + "\"}"));
    }

    private Employee employee(String code, String name) {
        Employee e = new Employee();
        e.setEmployeeCode(code);
        e.setFullName(name);
        e.setGender(Gender.FEMALE);
        e.setJoiningDate(LocalDate.now().minusMonths(2));
        return employees.saveAndFlush(e);
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
