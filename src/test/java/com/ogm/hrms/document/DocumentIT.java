package com.ogm.hrms.document;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Document Management against real PostgreSQL + local storage: upload,
 * list, download (binary round-trip), content-type rejection, RBAC, and delete.
 */
@AutoConfigureMockMvc
@Transactional
class DocumentIT extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Sup3rStr0ng!";
    private static final byte[] PDF_BYTES = "%PDF-1.4 fake content".getBytes(StandardCharsets.UTF_8);

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
        seedUser("docadmin@ogm.test", RoleName.SUPER_ADMIN);
        seedUser("docteamlead@ogm.test", RoleName.TEAM_LEAD); // DOCUMENT:VIEW only
    }

    @Test
    void uploadListDownloadDeleteLifecycle() throws Exception {
        String token = login("docadmin@ogm.test");

        MvcResult uploaded = mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "policy.pdf", "application/pdf", PDF_BYTES))
                        .param("documentType", "COMPANY_POLICY")
                        .param("title", "Leave Policy")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentType").value("COMPANY_POLICY"))
                .andExpect(jsonPath("$.data.sizeBytes").value(PDF_BYTES.length))
                .andReturn();
        int id = JsonPath.read(uploaded.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get("/api/v1/documents").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        MvcResult downloaded = mockMvc.perform(get("/api/v1/documents/" + id + "/download")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andReturn();
        assertThat(downloaded.getResponse().getContentAsByteArray()).isEqualTo(PDF_BYTES);

        mockMvc.perform(delete("/api/v1/documents/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/documents/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void unsupportedContentTypeIsRejected() throws Exception {
        String token = login("docadmin@ogm.test");
        mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "malware.exe", "application/x-msdownload", PDF_BYTES))
                        .param("documentType", "RESUME")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void viewerWithoutCreatePermissionCannotUpload() throws Exception {
        String token = login("docteamlead@ogm.test"); // TEAM_LEAD has DOCUMENT:VIEW, not CREATE
        mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "x.pdf", "application/pdf", PDF_BYTES))
                        .param("documentType", "RESUME")
                        .header("Authorization", "Bearer " + token))
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
