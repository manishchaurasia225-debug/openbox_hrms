package com.ogm.hrms.salary;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.EmployeeRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Salary & Compensation against real PostgreSQL: revision → payslip generation
 * (PDF into storage) → download, duplicate-period prevention, and RBAC.
 */
@AutoConfigureMockMvc
@Transactional
class SalaryIT extends AbstractPostgresIntegrationTest {

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

    private Long employeeId;

    @BeforeEach
    void seed() {
        seedUser("pay.hr@ogm.test", RoleName.HR_MANAGER);   // PAYROLL:CREATE + VIEW
        seedUser("pay.super@ogm.test", RoleName.SUPER_ADMIN); // PAYROLL:VIEW/EXPORT/ADMIN, NOT CREATE

        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-PAY-1");
        employee.setFullName("Payroll Person");
        employeeId = employees.saveAndFlush(employee).getId();
    }

    @Test
    void revisionThenGeneratePayslipAndDownload() throws Exception {
        String token = login("pay.hr@ogm.test");

        mockMvc.perform(post("/api/v1/salary/structures").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + ",\"effectiveFrom\":\"2026-01-01\","
                                + "\"basic\":50000.00,\"hra\":20000.00,\"specialAllowance\":10000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grossMonthly").value(80000.00));

        MvcResult generated = mockMvc.perform(post("/api/v1/salary/payslips").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + ",\"year\":2026,\"month\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grossPay").value(80000.00))
                .andExpect(jsonPath("$.data.netPay").value(80000.00))
                .andReturn();
        int payslipId = JsonPath.read(generated.getResponse().getContentAsString(), "$.data.id");

        // Duplicate period -> 409
        mockMvc.perform(post("/api/v1/salary/payslips").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + ",\"year\":2026,\"month\":7}"))
                .andExpect(status().isConflict());

        MvcResult pdf = mockMvc.perform(get("/api/v1/salary/payslips/" + payslipId + "/download")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        byte[] bytes = pdf.getResponse().getContentAsByteArray();
        assertThat(bytes).hasSizeGreaterThan(100);
        assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void superAdminCannotGeneratePayslips() throws Exception {
        String token = login("pay.super@ogm.test"); // Super Admin lacks PAYROLL:CREATE by design
        mockMvc.perform(post("/api/v1/salary/payslips").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + ",\"year\":2026,\"month\":8}"))
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
