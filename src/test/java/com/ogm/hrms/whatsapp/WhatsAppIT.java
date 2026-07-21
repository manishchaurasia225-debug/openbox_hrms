package com.ogm.hrms.whatsapp;

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

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for WhatsApp Integration against real PostgreSQL: seeded templates, a send that
 * records the ledger (simulated provider) and advances the delivery/read lifecycle, and RBAC.
 */
@AutoConfigureMockMvc
@Transactional
class WhatsAppIT extends AbstractPostgresIntegrationTest {

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
    void listsSeededTemplates() throws Exception {
        seedUser("wa.admin@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("wa.admin@ogm.test");

        mockMvc.perform(get("/api/v1/whatsapp/templates").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.code == 'WA_WELCOME')]").exists());
    }

    @Test
    void sendRecordsLedgerAndAdvancesDeliveryLifecycle() throws Exception {
        seedUser("wa.admin2@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("wa.admin2@ogm.test");
        Long templateId = createTemplate(token, "WA_TEST", "UTILITY", "Hi {name}, code {code}.");

        // Send — the logging provider accepts and returns a synthetic id.
        MvcResult sent = mockMvc.perform(post("/api/v1/whatsapp/templates/" + templateId + "/send")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toPhone\":\"+14155552671\",\"variables\":{\"name\":\"Alice\",\"code\":\"EMP-1\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SENT"))
                .andExpect(jsonPath("$.data.body").value("Hi Alice, code EMP-1."))
                .andExpect(jsonPath("$.data.providerMessageId", startsWith("sim-")))
                .andReturn();
        Integer messageId = JsonPath.read(sent.getResponse().getContentAsString(), "$.data.id");

        // Provider delivery callback.
        mockMvc.perform(patch("/api/v1/whatsapp/messages/" + messageId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERED"))
                .andExpect(jsonPath("$.data.deliveredAt").exists());

        // Read callback.
        mockMvc.perform(patch("/api/v1/whatsapp/messages/" + messageId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"READ\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READ"))
                .andExpect(jsonPath("$.data.readAt").exists());
    }

    @Test
    void deniesTemplateCreationWithoutCreateAuthority() throws Exception {
        seedUser("wa.emp@ogm.test", RoleName.EMPLOYEE);  // EMPLOYEE has WHATSAPP:VIEW/EDIT, not CREATE
        String token = login("wa.emp@ogm.test");

        mockMvc.perform(post("/api/v1/whatsapp/templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"X\",\"name\":\"X\",\"category\":\"UTILITY\",\"bodyText\":\"hi\"}"))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Long createTemplate(String token, String code, String category, String bodyText) throws Exception {
        String body = "{\"code\":\"" + code + "\",\"name\":\"" + code + "\",\"category\":\"" + category
                + "\",\"bodyText\":\"" + bodyText + "\"}";
        MvcResult result = mockMvc.perform(post("/api/v1/whatsapp/templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return ((Integer) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();
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
