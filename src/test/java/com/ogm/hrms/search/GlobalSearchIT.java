package com.ogm.hrms.search;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Department;
import com.ogm.hrms.entity.Designation;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.Gender;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.repository.DesignationRepository;
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
 * Integration tests for Global Search against real PostgreSQL: a single query returning typed,
 * grouped results across employees, departments, and designations.
 */
@AutoConfigureMockMvc
@Transactional
class GlobalSearchIT extends AbstractPostgresIntegrationTest {

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
    private DepartmentRepository departments;
    @Autowired
    private DesignationRepository designations;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void returnsGroupedTypedResults() throws Exception {
        seedUser("search.admin@ogm.test", RoleName.SUPER_ADMIN);
        employee("EMP-S1", "Alice Anderson");
        department("ALPHA", "Alpha");
        designation("ANL", "Analyst");
        String token = login("search.admin@ogm.test");

        // "al" appears in Alice, Alpha, and Analyst.
        mockMvc.perform(get("/api/v1/search?q=al").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.query").value("al"))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.groups[?(@.type == 'EMPLOYEE')]").exists())
                .andExpect(jsonPath("$.data.groups[?(@.type == 'DEPARTMENT')]").exists())
                .andExpect(jsonPath("$.data.groups[?(@.type == 'DESIGNATION')]").exists());
    }

    @Test
    void returnsEmptyWhenNothingMatches() throws Exception {
        seedUser("search.admin2@ogm.test", RoleName.SUPER_ADMIN);
        employee("EMP-S2", "Bob Brown");
        String token = login("search.admin2@ogm.test");

        mockMvc.perform(get("/api/v1/search?q=zzznomatch").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.groups").isEmpty());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Employee employee(String code, String name) {
        Employee e = new Employee();
        e.setEmployeeCode(code);
        e.setFullName(name);
        e.setGender(Gender.MALE);
        e.setJoiningDate(LocalDate.now().minusMonths(2));
        return employees.saveAndFlush(e);
    }

    private Department department(String code, String name) {
        Department d = new Department();
        d.setCode(code);
        d.setName(name);
        d.setActive(true);
        return departments.saveAndFlush(d);
    }

    private Designation designation(String code, String name) {
        Designation d = new Designation();
        d.setCode(code);
        d.setName(name);
        d.setActive(true);
        return designations.saveAndFlush(d);
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
