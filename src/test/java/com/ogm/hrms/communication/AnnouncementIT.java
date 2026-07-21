package com.ogm.hrms.communication;

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
 * Integration tests for Announcements against real PostgreSQL: draft → publish → feed visibility,
 * and RBAC.
 */
@AutoConfigureMockMvc
@Transactional
class AnnouncementIT extends AbstractPostgresIntegrationTest {

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
        // Publishing requires ANNOUNCEMENT:APPROVE, which Super Admin deliberately lacks (per the
        // permissions matrix). HR Manager holds CREATE + APPROVE.
        seedUser("ann.admin@ogm.test", RoleName.HR_MANAGER);
        seedUser("ann.employee@ogm.test", RoleName.EMPLOYEE);
    }

    @Test
    void draftThenPublishAppearsOnFeed() throws Exception {
        String token = login("ann.admin@ogm.test");

        MvcResult created = mockMvc.perform(post("/api/v1/announcements").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Town Hall\",\"body\":\"Friday 4pm\",\"category\":\"EVENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.published").value(false))
                .andReturn();
        int id = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        // Not yet published -> not on feed
        mockMvc.perform(get("/api/v1/announcements/feed").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(post("/api/v1/announcements/" + id + "/publish").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.published").value(true));

        mockMvc.perform(get("/api/v1/announcements/feed").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Town Hall"));
    }

    @Test
    void employeeCanReadFeedButNotCreate() throws Exception {
        String token = login("ann.employee@ogm.test");

        mockMvc.perform(get("/api/v1/announcements/feed").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/announcements").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Nope\",\"category\":\"NEWS\"}"))
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
