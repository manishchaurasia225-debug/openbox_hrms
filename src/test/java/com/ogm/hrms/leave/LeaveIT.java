package com.ogm.hrms.leave;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.LeaveType;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.LeaveBalanceRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Leave Management against real PostgreSQL: seeded leave types, apply →
 * two-level approval with balance deduction, insufficient-balance rejection, and the no-self-approval
 * business rule.
 */
@AutoConfigureMockMvc
@Transactional
class LeaveIT extends AbstractPostgresIntegrationTest {

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
    private LeaveBalanceRepository leaveBalances;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void applyThenTwoLevelApprovalDeductsBalance() throws Exception {
        Employee employee = createLinkedEmployee("leave.emp@ogm.test", RoleName.EMPLOYEE);
        seedUser("leave.admin@ogm.test", RoleName.SUPER_ADMIN);
        Long casualId = leaveTypeId("CASUAL");

        String empToken = login("leave.emp@ogm.test");
        LocalDate from = LocalDate.now();
        MvcResult applied = mockMvc.perform(post("/api/v1/leave/requests").header("Authorization", "Bearer " + empToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyJson(casualId, from, from.plusDays(1), "Trip")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.days").value(2.0))
                .andReturn();
        int id = JsonPath.read(applied.getResponse().getContentAsString(), "$.data.id");

        String adminToken = login("leave.admin@ogm.test");
        mockMvc.perform(post("/api/v1/leave/requests/" + id + "/approve").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MANAGER_APPROVED"));
        mockMvc.perform(post("/api/v1/leave/requests/" + id + "/approve").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        BigDecimal used = leaveBalances
                .findByEmployee_IdAndLeaveType_IdAndYear(employee.getId(), casualId, from.getYear())
                .orElseThrow().getUsed();
        assertThat(used).isEqualByComparingTo("2.0");
    }

    @Test
    void insufficientBalanceIsRejected() throws Exception {
        createLinkedEmployee("leave.greedy@ogm.test", RoleName.EMPLOYEE);
        Long casualId = leaveTypeId("CASUAL"); // quota 12
        String token = login("leave.greedy@ogm.test");
        LocalDate from = LocalDate.now();

        mockMvc.perform(post("/api/v1/leave/requests").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyJson(casualId, from, from.plusDays(19), "Long")))  // 20 days > 12
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannotApproveOwnLeave() throws Exception {
        // HR_MANAGER has both LEAVE:CREATE and LEAVE:APPROVE, so the @PreAuthorize passes and the
        // service-level no-self-approval rule is what must reject the request.
        createLinkedEmployee("leave.hr@ogm.test", RoleName.HR_MANAGER);
        Long casualId = leaveTypeId("CASUAL");
        String token = login("leave.hr@ogm.test");
        LocalDate from = LocalDate.now();

        MvcResult applied = mockMvc.perform(post("/api/v1/leave/requests").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyJson(casualId, from, from, "Personal")))
                .andExpect(status().isOk())
                .andReturn();
        int id = JsonPath.read(applied.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(post("/api/v1/leave/requests/" + id + "/approve").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Long leaveTypeId(String code) {
        return leaveTypes.findByDeletedFalseOrderByName().stream()
                .filter(t -> t.getCode().equalsIgnoreCase(code))
                .map(LeaveType::getId).findFirst().orElseThrow();
    }

    private String applyJson(Long typeId, LocalDate from, LocalDate to, String reason) {
        return "{\"leaveTypeId\":" + typeId + ",\"fromDate\":\"" + from + "\",\"toDate\":\"" + to
                + "\",\"reason\":\"" + reason + "\"}";
    }

    private Employee createLinkedEmployee(String email, RoleName roleName) {
        User user = seedUser(email, roleName);
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-" + Math.abs(email.hashCode()));
        employee.setFullName("Leave Person");
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
