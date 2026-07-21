package com.ogm.hrms.user;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for administrative user management against real PostgreSQL (Testcontainers),
 * exercising RBAC (@PreAuthorize) and the "only Super Admin can assign administrative/HR roles"
 * business rule through the HTTP layer.
 */
@AutoConfigureMockMvc
@Transactional
class UserAdminIT extends AbstractPostgresIntegrationTest {

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
        createUser("super@ogm.test", RoleName.SUPER_ADMIN);
        createUser("hrexec@ogm.test", RoleName.HR_EXECUTIVE);
        createUser("employee@ogm.test", RoleName.EMPLOYEE);
    }

    @Test
    void superAdminCanCreateAnEmployeeUser() throws Exception {
        String token = login("super@ogm.test");

        mockMvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson("new.emp@ogm.test", "EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new.emp@ogm.test"))
                .andExpect(jsonPath("$.data.roles[0]").value("EMPLOYEE"));

        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    void hrExecutiveCannotAssignPrivilegedRole() throws Exception {
        String token = login("hrexec@ogm.test");

        // HR Executive has AUTH:CREATE so passes @PreAuthorize, but may not assign HR/admin roles.
        mockMvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson("sneaky.hr@ogm.test", "HR_MANAGER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // ...but can create a plain employee.
        mockMvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson("hr.made.emp@ogm.test", "EMPLOYEE")))
                .andExpect(status().isOk());
    }

    @Test
    void employeeWithoutPermissionCannotCreateUsers() throws Exception {
        String token = login("employee@ogm.test");

        mockMvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson("nope@ogm.test", "EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private void createUser(String email, RoleName roleName) {
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

    private String createUserJson(String email, String role) {
        return "{\"email\":\"" + email + "\",\"fullName\":\"New User\",\"password\":\"" + PASSWORD
                + "\",\"roles\":[\"" + role + "\"]}";
    }
}
