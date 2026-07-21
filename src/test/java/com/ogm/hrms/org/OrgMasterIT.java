package com.ogm.hrms.org;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Organization master data (Department/Designation) against real
 * PostgreSQL: full CRUD lifecycle, uniqueness conflicts, and RBAC enforcement.
 */
@AutoConfigureMockMvc
@Transactional
class OrgMasterIT extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Sup3rStr0ng!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository users;
    @Autowired
    private RoleRepository roles;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedActors() {
        seedUser("orgadmin@ogm.test", RoleName.SUPER_ADMIN);
        seedUser("orgemployee@ogm.test", RoleName.EMPLOYEE);
    }

    @Test
    void departmentCrudLifecycle() throws Exception {
        String token = login("orgadmin@ogm.test");

        MvcResult created = mockMvc.perform(post("/api/v1/departments").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ENG\",\"name\":\"Engineering\",\"description\":\"Builds things\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("ENG"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andReturn();
        int id = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        // Duplicate code -> 409
        mockMvc.perform(post("/api/v1/departments").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ENG\",\"name\":\"Engineering 2\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/departments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(put("/api/v1/departments/" + id).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ENG\",\"name\":\"Engineering\",\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(delete("/api/v1/departments/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/departments/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void designationCanBeCreated() throws Exception {
        String token = login("orgadmin@ogm.test");
        mockMvc.perform(post("/api/v1/designations").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SDE\",\"name\":\"Software Engineer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Software Engineer"));
    }

    @Test
    void employeeCanViewButNotModifyDepartments() throws Exception {
        String token = login("orgemployee@ogm.test");

        // EMPLOYEE has DEPARTMENT:VIEW ...
        mockMvc.perform(get("/api/v1/departments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // ... but not DEPARTMENT:CREATE.
        mockMvc.perform(post("/api/v1/departments").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"X\",\"name\":\"Nope\"}"))
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
