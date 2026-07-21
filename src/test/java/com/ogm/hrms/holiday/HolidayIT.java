package com.ogm.hrms.holiday;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Holiday Management against real PostgreSQL: CRUD, duplicate prevention,
 * year calendar, and RBAC.
 */
@AutoConfigureMockMvc
@Transactional
class HolidayIT extends AbstractPostgresIntegrationTest {

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
        seedUser("hol.admin@ogm.test", RoleName.SUPER_ADMIN);
        seedUser("hol.employee@ogm.test", RoleName.EMPLOYEE);
    }

    @Test
    void holidayCrudAndCalendar() throws Exception {
        String token = login("hol.admin@ogm.test");

        MvcResult created = mockMvc.perform(post("/api/v1/holidays").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"holidayDate\":\"2026-01-26\",\"name\":\"Republic Day\",\"type\":\"NATIONAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Republic Day"))
                .andReturn();
        int id = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        // Duplicate date+name -> 409
        mockMvc.perform(post("/api/v1/holidays").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"holidayDate\":\"2026-01-26\",\"name\":\"Republic Day\",\"type\":\"NATIONAL\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/holidays?year=2026").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Republic Day"));

        mockMvc.perform(delete("/api/v1/holidays/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/holidays/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeeCanViewButNotCreate() throws Exception {
        String token = login("hol.employee@ogm.test");

        mockMvc.perform(get("/api/v1/holidays?year=2026").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/holidays").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"holidayDate\":\"2026-05-01\",\"name\":\"May Day\",\"type\":\"NATIONAL\"}"))
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
