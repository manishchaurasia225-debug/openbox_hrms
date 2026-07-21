package com.ogm.hrms.dashboard;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Analytics dashboards against real PostgreSQL: the HR aggregate dashboard
 * and the employee self-service dashboard.
 */
@AutoConfigureMockMvc
@Transactional
class DashboardIT extends AbstractPostgresIntegrationTest {

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
    void hrDashboardAggregates() throws Exception {
        seedUser("dash.admin@ogm.test", RoleName.SUPER_ADMIN);
        employee("EMP-D1", "Alice", Gender.FEMALE, LocalDate.now().minusDays(5));  // recent joiner
        employee("EMP-D2", "Bob", Gender.MALE, LocalDate.now().minusYears(2));

        String token = login("dash.admin@ogm.test");
        mockMvc.perform(get("/api/v1/dashboard/hr").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEmployees").value(2))
                .andExpect(jsonPath("$.data.newJoinersLast30Days").value(1))
                .andExpect(jsonPath("$.data.genderDistribution").isArray());
    }

    @Test
    void employeeDashboardReturnsSelfSummary() throws Exception {
        User user = seedUser("dash.emp@ogm.test", RoleName.EMPLOYEE);
        Employee e = employee("EMP-D3", "Carol", Gender.FEMALE, LocalDate.now().minusMonths(3));
        e.setUser(user);
        employees.saveAndFlush(e);

        String token = login("dash.emp@ogm.test");
        mockMvc.perform(get("/api/v1/dashboard/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeName").value("Carol"))
                .andExpect(jsonPath("$.data.profileCompletionPercent").isNumber());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Employee employee(String code, String name, Gender gender, LocalDate joiningDate) {
        Employee e = new Employee();
        e.setEmployeeCode(code);
        e.setFullName(name);
        e.setGender(gender);
        e.setJoiningDate(joiningDate);
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
