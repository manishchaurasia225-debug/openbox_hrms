package com.ogm.hrms.communication;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.LeaveType;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.LeaveTypeRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Notification Center against real PostgreSQL: admin send → recipient
 * center (list/unread-count/read), and the leave-approval → notification producer wiring.
 */
@AutoConfigureMockMvc
@Transactional
class NotificationIT extends AbstractPostgresIntegrationTest {

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
    private LeaveTypeRepository leaveTypes;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminSendsThenRecipientReadsAndClears() throws Exception {
        User recipient = seedUser("notif.rcpt@ogm.test", RoleName.EMPLOYEE);
        seedUser("notif.admin@ogm.test", RoleName.SUPER_ADMIN);

        String adminToken = login("notif.admin@ogm.test");
        mockMvc.perform(post("/api/v1/notifications/send").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + recipient.getId() + ",\"title\":\"Welcome\",\"message\":\"Hello!\"}"))
                .andExpect(status().isOk());

        String rcptToken = login("notif.rcpt@ogm.test");
        mockMvc.perform(get("/api/v1/notifications/unread-count").header("Authorization", "Bearer " + rcptToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unread").value(1));

        MvcResult list = mockMvc.perform(get("/api/v1/notifications").header("Authorization", "Bearer " + rcptToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Welcome"))
                .andReturn();
        int id = JsonPath.read(list.getResponse().getContentAsString(), "$.data.content[0].id");

        mockMvc.perform(post("/api/v1/notifications/" + id + "/read").header("Authorization", "Bearer " + rcptToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read").value(true));

        mockMvc.perform(get("/api/v1/notifications/unread-count").header("Authorization", "Bearer " + rcptToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unread").value(0));
    }

    @Test
    void leaveApprovalNotifiesTheEmployee() throws Exception {
        Employee employee = createLinkedEmployee("notif.leave@ogm.test", RoleName.EMPLOYEE);
        seedUser("notif.hr@ogm.test", RoleName.SUPER_ADMIN);
        Long casualId = leaveTypes.findByDeletedFalseOrderByName().stream()
                .filter(t -> t.getCode().equals("CASUAL")).map(LeaveType::getId).findFirst().orElseThrow();

        String empToken = login("notif.leave@ogm.test");
        LocalDate from = LocalDate.now();
        MvcResult applied = mockMvc.perform(post("/api/v1/leave/requests").header("Authorization", "Bearer " + empToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leaveTypeId\":" + casualId + ",\"fromDate\":\"" + from + "\",\"toDate\":\"" + from + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        int reqId = JsonPath.read(applied.getResponse().getContentAsString(), "$.data.id");

        String hrToken = login("notif.hr@ogm.test");
        mockMvc.perform(post("/api/v1/leave/requests/" + reqId + "/approve").header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/leave/requests/" + reqId + "/approve").header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isOk());

        // The employee received an in-app "Leave approved" notification.
        mockMvc.perform(get("/api/v1/notifications").header("Authorization", "Bearer " + empToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Leave approved"))
                .andExpect(jsonPath("$.data.content[0].referenceType").value("LEAVE_REQUEST"));
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Employee createLinkedEmployee(String email, RoleName roleName) {
        User user = seedUser(email, roleName);
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-" + Math.abs(email.hashCode()));
        employee.setFullName("Notif Person");
        employee.setUser(user);
        return employees.saveAndFlush(employee);
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
