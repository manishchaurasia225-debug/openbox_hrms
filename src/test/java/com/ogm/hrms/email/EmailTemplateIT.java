package com.ogm.hrms.email;

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
 * Integration tests for the Email Template Engine against real PostgreSQL: the seeded starter
 * templates, create + placeholder rendering (preview), and RBAC on template creation.
 */
@AutoConfigureMockMvc
@Transactional
class EmailTemplateIT extends AbstractPostgresIntegrationTest {

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
    void listsSeededStarterTemplates() throws Exception {
        seedUser("mail.admin@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("mail.admin@ogm.test");

        mockMvc.perform(get("/api/v1/email-templates").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.code == 'WELCOME')]").exists())
                .andExpect(jsonPath("$.data[?(@.code == 'LEAVE_APPROVED')]").exists());
    }

    @Test
    void createsAndPreviewsTemplateWithVariables() throws Exception {
        seedUser("mail.admin2@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("mail.admin2@ogm.test");

        String body = "{\"code\":\"BIRTHDAY_MAIL\",\"name\":\"Birthday\",\"category\":\"ENGAGEMENT\","
                + "\"subject\":\"Happy Birthday, {name}!\","
                + "\"bodyHtml\":\"<p>Dear {name} ({code}), have a great day!</p>\"}";
        MvcResult created = mockMvc.perform(post("/api/v1/email-templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("BIRTHDAY_MAIL"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andReturn();
        Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(post("/api/v1/email-templates/" + id + "/preview")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variables\":{\"name\":\"Alice\",\"code\":\"EMP-1\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subject").value("Happy Birthday, Alice!"))
                .andExpect(jsonPath("$.data.bodyHtml").value("<p>Dear Alice (EMP-1), have a great day!</p>"));
    }

    @Test
    void deniesTemplateCreationWithoutEmailCreateAuthority() throws Exception {
        seedUser("mail.emp@ogm.test", RoleName.EMPLOYEE);  // EMPLOYEE has no EMAIL grants
        String token = login("mail.emp@ogm.test");

        String body = "{\"code\":\"X\",\"name\":\"X\",\"category\":\"GENERAL\",\"subject\":\"s\",\"bodyHtml\":\"b\"}";
        mockMvc.perform(post("/api/v1/email-templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
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
