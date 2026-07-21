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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Company Profile and System Settings against real PostgreSQL: the seeded
 * singletons/defaults are readable and editable by admins, protected settings, and RBAC.
 */
@AutoConfigureMockMvc
@Transactional
class CompanySettingsIT extends AbstractPostgresIntegrationTest {

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
        seedUser("cadmin@ogm.test", RoleName.SUPER_ADMIN);
        seedUser("cemployee@ogm.test", RoleName.EMPLOYEE);
    }

    @Test
    void adminCanReadAndUpdateCompanyAndSettings() throws Exception {
        String token = login("cadmin@ogm.test");

        // Seeded company profile is readable.
        mockMvc.perform(get("/api/v1/company").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("OGM HRMS"));

        // ...and editable.
        mockMvc.perform(put("/api/v1/company").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"legalName\":\"OGM Technologies Pvt Ltd\",\"displayName\":\"OGM\",\"currency\":\"INR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("OGM"))
                .andExpect(jsonPath("$.data.legalName").value("OGM Technologies Pvt Ltd"));

        // Seeded attendance-policy setting is readable and editable.
        mockMvc.perform(get("/api/v1/settings/attendance.working-hours-per-day")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value("8"));

        mockMvc.perform(put("/api/v1/settings/attendance.max-wfh-days-per-month")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.value").value("12"));

        // Filter by category returns the attendance settings.
        mockMvc.perform(get("/api/v1/settings?category=attendance").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].category").value("attendance"));
    }

    @Test
    void employeeCanViewButNotModify() throws Exception {
        String token = login("cemployee@ogm.test");

        mockMvc.perform(get("/api/v1/company").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/company").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"legalName\":\"Nope\",\"displayName\":\"Nope\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/settings").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/settings").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"x.y\",\"value\":\"1\"}"))
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
