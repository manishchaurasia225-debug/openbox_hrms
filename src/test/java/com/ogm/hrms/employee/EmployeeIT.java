package com.ogm.hrms.employee;

import com.jayway.jsonpath.JsonPath;
import com.ogm.hrms.entity.Department;
import com.ogm.hrms.entity.Designation;
import com.ogm.hrms.entity.EmploymentType;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.repository.DesignationRepository;
import com.ogm.hrms.repository.EmploymentTypeRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Employee master against real PostgreSQL: CRUD lifecycle, organization
 * FK resolution, unique code, unknown-reference handling, and RBAC.
 */
@AutoConfigureMockMvc
@Transactional
class EmployeeIT extends AbstractPostgresIntegrationTest {

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
    private DepartmentRepository departments;
    @Autowired
    private DesignationRepository designations;
    @Autowired
    private EmploymentTypeRepository employmentTypes;

    private Long deptId;
    private Long desigId;
    private Long typeId;

    @BeforeEach
    void seed() {
        seedUser("empadmin@ogm.test", RoleName.SUPER_ADMIN);
        seedUser("empself@ogm.test", RoleName.EMPLOYEE);

        Department dept = new Department();
        dept.setCode("ENG");
        dept.setName("Engineering");
        deptId = departments.saveAndFlush(dept).getId();

        Designation desig = new Designation();
        desig.setCode("SDE");
        desig.setName("Software Engineer");
        desigId = designations.saveAndFlush(desig).getId();

        EmploymentType type = new EmploymentType();
        type.setCode("FT");
        type.setName("Full-Time");
        typeId = employmentTypes.saveAndFlush(type).getId();
    }

    @Test
    void employeeCrudLifecycleWithOrgReferences() throws Exception {
        String token = login("empadmin@ogm.test");

        MvcResult created = mockMvc.perform(post("/api/v1/employees").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("EMP001", "Jane Doe")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.data.employment.departmentName").value("Engineering"))
                .andExpect(jsonPath("$.data.employment.designationName").value("Software Engineer"))
                .andExpect(jsonPath("$.data.salary.basicSalary").exists())
                .andReturn();
        int id = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        // Duplicate code -> 409
        mockMvc.perform(post("/api/v1/employees").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("EMP001", "Someone Else")))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/employees").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(put("/api/v1/employees/" + id).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("EMP001", "Jane R. Doe")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Jane R. Doe"));

        mockMvc.perform(delete("/api/v1/employees/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/employees/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownDepartmentIsRejected() throws Exception {
        String token = login("empadmin@ogm.test");
        String body = "{\"employeeCode\":\"EMP900\",\"fullName\":\"Bad Ref\","
                + "\"employment\":{\"departmentId\":999999}}";
        mockMvc.perform(post("/api/v1/employees").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void plainEmployeeCannotCreateEmployees() throws Exception {
        String token = login("empself@ogm.test"); // EMPLOYEE role: VIEW/EDIT but not CREATE
        mockMvc.perform(post("/api/v1/employees").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("EMP777", "Nope")))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private String employeeJson(String code, String name) {
        return "{"
                + "\"employeeCode\":\"" + code + "\","
                + "\"fullName\":\"" + name + "\","
                + "\"gender\":\"FEMALE\","
                + "\"contact\":{\"mobile\":\"9999999999\",\"officialEmail\":\"jane@ogm.test\"},"
                + "\"employment\":{\"departmentId\":" + deptId + ",\"designationId\":" + desigId
                + ",\"employmentTypeId\":" + typeId + ",\"employmentStatus\":\"ACTIVE\"},"
                + "\"salary\":{\"basicSalary\":50000.00,\"hra\":20000.00}"
                + "}";
    }

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
