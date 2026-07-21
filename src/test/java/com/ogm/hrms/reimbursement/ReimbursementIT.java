package com.ogm.hrms.reimbursement;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.EmployeeRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Reimbursement against real PostgreSQL: submit → two-level (manager→finance)
 * approval → pay, and the no-self-approval business rule.
 */
@AutoConfigureMockMvc
@Transactional
class ReimbursementIT extends AbstractPostgresIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @Test
    void submitApproveTwiceThenPay() throws Exception {
        createLinkedEmployee("exp.emp@ogm.test", RoleName.EMPLOYEE);
        createLinkedEmployee("exp.finance@ogm.test", RoleName.FINANCE); // EXPENSE:APPROVE

        String empToken = login("exp.emp@ogm.test");
        MvcResult submitted = mockMvc.perform(post("/api/v1/reimbursements").header("Authorization", "Bearer " + empToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"TRAVEL\",\"amount\":1500.00,\"expenseDate\":\"" + LocalDate.now()
                                + "\",\"description\":\"Cab\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andReturn();
        int id = JsonPath.read(submitted.getResponse().getContentAsString(), "$.data.id");

        String finToken = login("exp.finance@ogm.test");
        mockMvc.perform(post("/api/v1/reimbursements/" + id + "/approve").header("Authorization", "Bearer " + finToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MANAGER_APPROVED"));
        mockMvc.perform(post("/api/v1/reimbursements/" + id + "/approve").header("Authorization", "Bearer " + finToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
        mockMvc.perform(post("/api/v1/reimbursements/" + id + "/pay").header("Authorization", "Bearer " + finToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.paidAt").isNotEmpty());
    }

    @Test
    void cannotApproveOwnClaim() throws Exception {
        // A user with EMPLOYEE (EXPENSE:CREATE) + FINANCE (EXPENSE:APPROVE) passes @PreAuthorize on both
        // endpoints, so the service-level no-self-approval rule is what must reject.
        createLinkedEmployee("exp.self@ogm.test", RoleName.EMPLOYEE, RoleName.FINANCE);
        String token = login("exp.self@ogm.test");

        MvcResult submitted = mockMvc.perform(post("/api/v1/reimbursements").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"FOOD\",\"amount\":300.00,\"expenseDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        int id = JsonPath.read(submitted.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(post("/api/v1/reimbursements/" + id + "/approve").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Employee createLinkedEmployee(String email, RoleName... roleNames) {
        User user = seedUser(email, roleNames);
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-" + Math.abs(email.hashCode()));
        employee.setFullName("Expense Person");
        employee.setUser(user);
        return employees.saveAndFlush(employee);
    }

    private User seedUser(String email, RoleName... roleNames) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setFullName("Test User");
        user.setEnabled(true);
        user.setEmailVerified(true);
        for (RoleName roleName : roleNames) {
            user.addRole(roles.findByName(roleName).orElseThrow());
        }
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
