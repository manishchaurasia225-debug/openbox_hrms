package com.ogm.hrms.employee;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for employee profile sub-resources (emergency contacts, family, education,
 * experience, timeline) against real PostgreSQL.
 */
@AutoConfigureMockMvc
@Transactional
class EmployeeProfileIT extends AbstractPostgresIntegrationTest {

    private static final String PASSWORD = "Sup3rStr0ng!";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository users;
    @Autowired
    private RoleRepository roles;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmployeeRepository employees;

    private Long employeeId;

    @BeforeEach
    void seed() {
        Role role = roles.findByName(RoleName.SUPER_ADMIN).orElseThrow();
        User user = new User();
        user.setEmail("profadmin@ogm.test");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setFullName("Prof Admin");
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.addRole(role);
        users.saveAndFlush(user);

        Employee employee = new Employee();
        employee.setEmployeeCode("EMP100");
        employee.setFullName("Profile Person");
        employeeId = employees.saveAndFlush(employee).getId();
    }

    @Test
    void emergencyContactFullLifecycle() throws Exception {
        String token = login();

        MvcResult created = mockMvc.perform(post(base() + "/emergency-contacts").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"relationship\":\"Brother\",\"phone\":\"12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John"))
                .andReturn();
        int id = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get(base() + "/emergency-contacts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("John"));

        mockMvc.perform(delete(base() + "/emergency-contacts/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get(base() + "/emergency-contacts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void addsFamilyEducationExperienceAndTimeline() throws Exception {
        String token = login();

        mockMvc.perform(post(base() + "/family").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mary\",\"relationship\":\"Mother\",\"dependent\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dependent").value(true));

        mockMvc.perform(post(base() + "/education").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institution\":\"MIT\",\"degree\":\"BSc\",\"endYear\":2018}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.institution").value("MIT"));

        mockMvc.perform(post(base() + "/experience").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyName\":\"Acme\",\"designation\":\"Dev\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("Acme"));

        mockMvc.perform(post(base() + "/timeline").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventType\":\"NOTE\",\"title\":\"Onboarded\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get(base() + "/timeline").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Onboarded"));
    }

    @Test
    void unknownEmployeeReturns404() throws Exception {
        String token = login();
        mockMvc.perform(post("/api/v1/employees/999999/emergency-contacts").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ghost\"}"))
                .andExpect(status().isNotFound());
    }

    private String base() {
        return "/api/v1/employees/" + employeeId;
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"profadmin@ogm.test\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }
}
