package com.ogm.hrms.automation;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.Gender;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.NotificationRepository;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Automation Engine against real PostgreSQL: the seeded rule catalogue,
 * a birthday dispatch end-to-end (recipients matched → notifications persisted), rule configuration,
 * and RBAC on manual triggers.
 */
@AutoConfigureMockMvc
@Transactional
class AutomationIT extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Sup3rStr0ng!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository users;
    @Autowired
    private RoleRepository roles;
    @Autowired
    private EmployeeRepository employees;
    @Autowired
    private NotificationRepository notifications;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void listRulesReturnsSeededCatalogue() throws Exception {
        seedUser("auto.admin@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("auto.admin@ogm.test");

        mockMvc.perform(get("/api/v1/automations").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(9));
    }

    @Test
    void runBirthdayWishDispatchesNotifications() throws Exception {
        seedUser("auto.admin2@ogm.test", RoleName.SUPER_ADMIN);
        User employeeUser = seedUser("birthday.emp@ogm.test", RoleName.EMPLOYEE);
        Employee e = employee("EMP-A1", "Carol", LocalDate.now());  // birthday is today
        e.setUser(employeeUser);
        employees.saveAndFlush(e);

        String token = login("auto.admin2@ogm.test");
        mockMvc.perform(post("/api/v1/automations/BIRTHDAY_WISH/run").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("BIRTHDAY_WISH"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.matched").value(1))
                .andExpect(jsonPath("$.data.dispatched").value(2));  // IN_APP + EMAIL

        // Both channels persist a notification, so the recipient has two unread items.
        assertThat(notifications.countByRecipient_IdAndReadFalseAndDeletedFalse(employeeUser.getId()))
                .isEqualTo(2);
    }

    @Test
    void updateRuleTogglesEnabled() throws Exception {
        seedUser("auto.admin3@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("auto.admin3@ogm.test");

        mockMvc.perform(patch("/api/v1/automations/ATTENDANCE_REMINDER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("ATTENDANCE_REMINDER"))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void deniesManualRunWithoutAdminAuthority() throws Exception {
        seedUser("auto.emp@ogm.test", RoleName.EMPLOYEE);  // has NOTIFICATION:VIEW, not ADMIN
        String token = login("auto.emp@ogm.test");

        mockMvc.perform(post("/api/v1/automations/BIRTHDAY_WISH/run")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Employee employee(String code, String name, LocalDate dateOfBirth) {
        Employee e = new Employee();
        e.setEmployeeCode(code);
        e.setFullName(name);
        e.setGender(Gender.FEMALE);
        e.setDateOfBirth(dateOfBirth);
        e.setJoiningDate(LocalDate.now().minusYears(1));
        return employees.saveAndFlush(e);
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
