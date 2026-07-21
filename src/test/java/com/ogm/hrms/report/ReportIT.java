package com.ogm.hrms.report;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.Gender;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for report export against real PostgreSQL: verifies CSV/Excel/PDF rendering of
 * the employee report and RBAC enforcement of {@code REPORT:EXPORT}.
 */
@AutoConfigureMockMvc
@Transactional
class ReportIT extends AbstractPostgresIntegrationTest {

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
    void exportsEmployeeReportAsCsv() throws Exception {
        seedUser("rep.admin@ogm.test", RoleName.SUPER_ADMIN);
        employee("EMP-R1", "Alice", Gender.FEMALE);
        String token = login("rep.admin@ogm.test");

        MvcResult result = mockMvc.perform(get("/api/v1/reports/EMPLOYEE?format=CSV")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Employee_Report.csv\""))
                .andReturn();

        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("Code,Name,Gender").contains("EMP-R1").contains("Alice");
    }

    @Test
    void exportsEmployeeReportAsExcel() throws Exception {
        seedUser("rep.xls@ogm.test", RoleName.SUPER_ADMIN);
        employee("EMP-R2", "Bob", Gender.MALE);
        String token = login("rep.xls@ogm.test");

        MvcResult result = mockMvc.perform(get("/api/v1/reports/EMPLOYEE?format=EXCEL")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        // XLSX is a ZIP archive: first two bytes are the "PK" local-file-header signature.
        assertThat(bytes.length).isGreaterThan(0);
        assertThat(bytes[0]).isEqualTo((byte) 'P');
        assertThat(bytes[1]).isEqualTo((byte) 'K');
    }

    @Test
    void exportsAttendanceReportAsPdf() throws Exception {
        seedUser("rep.pdf@ogm.test", RoleName.SUPER_ADMIN);
        String token = login("rep.pdf@ogm.test");

        MvcResult result = mockMvc.perform(get("/api/v1/reports/ATTENDANCE?format=PDF")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        // PDF files begin with the "%PDF" magic header.
        assertThat(new String(bytes, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void deniesExportWithoutPermission() throws Exception {
        seedUser("rep.lead@ogm.test", RoleName.TEAM_LEAD);  // has REPORT:VIEW but not REPORT:EXPORT
        String token = login("rep.lead@ogm.test");

        mockMvc.perform(get("/api/v1/reports/EMPLOYEE?format=CSV")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Employee employee(String code, String name, Gender gender) {
        Employee e = new Employee();
        e.setEmployeeCode(code);
        e.setFullName(name);
        e.setGender(gender);
        e.setJoiningDate(LocalDate.now().minusMonths(1));
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
