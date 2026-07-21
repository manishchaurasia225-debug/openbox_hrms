package com.ogm.hrms.audit;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.repository.RoleRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Audit Log module against real PostgreSQL. NOT {@code @Transactional}:
 * audit writes run in their own transaction ({@code REQUIRES_NEW}) and must commit independently, so
 * the test relies on unique per-test emails rather than rollback for isolation.
 */
@AutoConfigureMockMvc
class AuditIT extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Sup3rStr0ng!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository users;
    @Autowired
    private RoleRepository roles;
    @Autowired
    private DepartmentRepository departments;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // This IT is intentionally non-transactional (audit writes commit via REQUIRES_NEW), so any
    // committed business entities must be cleaned up to avoid polluting other tests' assertions.
    @AfterEach
    void cleanUpCreatedDepartments() {
        departments.findByDeletedFalseOrderByNameAsc().stream()
                .filter(d -> "AUD-DEP".equalsIgnoreCase(d.getCode()))
                .forEach(departments::delete);
    }

    @Test
    void recordsSuccessfulLogin() throws Exception {
        String email = "audit.login@ogm.test";
        seedUser(email, RoleName.SUPER_ADMIN);
        String token = login(email);

        mockMvc.perform(get("/api/v1/audit-logs?action=LOGIN&actorEmail=" + email)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].action").value("LOGIN"))
                .andExpect(jsonPath("$.data.content[0].outcome").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].actorEmail").value(email));
    }

    @Test
    void recordsFailedLogin() throws Exception {
        String email = "audit.fail@ogm.test";
        seedUser(email, RoleName.SUPER_ADMIN);
        // Wrong password → 401, but the attempt must still be audited (REQUIRES_NEW).
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"wrong-pass\"}"))
                .andExpect(status().isUnauthorized());

        String token = login(email);  // now a real login to obtain a token for querying
        mockMvc.perform(get("/api/v1/audit-logs?action=LOGIN_FAILED&actorEmail=" + email)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].action").value("LOGIN_FAILED"))
                .andExpect(jsonPath("$.data.content[0].outcome").value("FAILURE"));
    }

    @Test
    void capturesMutatingHttpRequests() throws Exception {
        seedUser("audit.admin@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("audit.admin@ogm.test");

        // A mutating request the interceptor should audit as CREATE on the 'departments' resource.
        mockMvc.perform(post("/api/v1/departments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"AUD-DEP\",\"name\":\"Audit Dept\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit-logs?action=CREATE&module=departments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].action").value("CREATE"))
                .andExpect(jsonPath("$.data.content[0].module").value("departments"))
                .andExpect(jsonPath("$.data.content[0].httpMethod").value("POST"));
    }

    @Test
    void deniesAuditAccessWithoutPermission() throws Exception {
        seedUser("audit.emp@ogm.test", RoleName.EMPLOYEE);  // EMPLOYEE has no AUDIT grants
        String token = login("audit.emp@ogm.test");

        mockMvc.perform(get("/api/v1/audit-logs").header("Authorization", "Bearer " + token))
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
