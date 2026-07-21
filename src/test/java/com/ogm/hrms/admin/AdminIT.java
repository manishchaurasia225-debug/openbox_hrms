package com.ogm.hrms.admin;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the System Administration console against real PostgreSQL: system info,
 * the role→permission catalogue, login history, and RBAC.
 */
@AutoConfigureMockMvc
@Transactional
class AdminIT extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Sup3rStr0ng!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository users;
    @Autowired
    private RoleRepository roles;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void reportsSystemInfo() throws Exception {
        seedUser("admin.info@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("admin.info@ogm.test");

        mockMvc.perform(get("/api/v1/admin/system-info").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationName").isNotEmpty())
                .andExpect(jsonPath("$.data.databaseStatus").value("UP"))
                .andExpect(jsonPath("$.data.counts.roles").value(RoleName.values().length))
                .andExpect(jsonPath("$.data.uptimeSeconds").isNumber());
    }

    @Test
    void returnsRolePermissionCatalogue() throws Exception {
        seedUser("admin.roles@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("admin.roles@ogm.test");

        mockMvc.perform(get("/api/v1/admin/roles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(RoleName.values().length))
                .andExpect(jsonPath("$.data[?(@.role == 'SUPER_ADMIN')].permissionCount").exists());
    }

    @Test
    void returnsLoginHistory() throws Exception {
        String email = "admin.hist@ogm.test";
        seedUser(email, RoleName.SUPER_ADMIN);
        String token = login(email);  // records a successful login attempt

        mockMvc.perform(get("/api/v1/admin/login-history?email=" + email)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value(email))
                .andExpect(jsonPath("$.data.content[0].successful").value(true));
    }

    @Test
    void deniesSystemInfoWithoutAdminAuthority() throws Exception {
        seedUser("admin.emp@ogm.test", RoleName.EMPLOYEE);  // has SETTINGS:VIEW/EDIT, not ADMIN
        String token = login("admin.emp@ogm.test");

        mockMvc.perform(get("/api/v1/admin/system-info").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---------------------------------------------------------------------------------

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
