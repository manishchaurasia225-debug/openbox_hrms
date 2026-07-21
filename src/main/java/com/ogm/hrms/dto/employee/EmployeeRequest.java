package com.ogm.hrms.dto.employee;

import com.ogm.hrms.enums.EmploymentStatus;
import com.ogm.hrms.enums.Gender;
import com.ogm.hrms.enums.MaritalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Create/update payload for an employee. Fields are grouped into nested records mirroring the
 * employee master sections. Only {@code employeeCode} and {@code fullName} are required; all other
 * sections are optional and may be filled in progressively.
 */
public record EmployeeRequest(
        @NotBlank @Size(max = 40) String employeeCode,
        @NotBlank @Size(max = 150) String fullName,
        Gender gender,
        LocalDate dateOfBirth,
        @Size(max = 10) String bloodGroup,
        @Size(max = 60) String nationality,
        MaritalStatus maritalStatus,
        @Size(max = 500) String photoUrl,
        Contact contact,
        Employment employment,
        Salary salary,
        Bank bank,
        GovernmentIds governmentIds,
        Social social,
        Long userId
) {

    public record Contact(
            @Size(max = 30) String mobile,
            @Size(max = 190) String personalEmail,
            @Size(max = 190) String officialEmail,
            @Size(max = 300) String currentAddress,
            @Size(max = 300) String permanentAddress) {}

    public record Employment(
            Long departmentId,
            Long designationId,
            Long employmentTypeId,
            LocalDate joiningDate,
            LocalDate endDate,
            Integer noticePeriodDays,
            EmploymentStatus employmentStatus) {}

    public record Salary(
            BigDecimal basicSalary,
            BigDecimal hra,
            BigDecimal specialAllowance,
            BigDecimal bonus,
            BigDecimal incentives,
            BigDecimal otherAllowances) {}

    public record Bank(
            @Size(max = 120) String bankName,
            @Size(max = 40) String accountNumber,
            @Size(max = 20) String ifscCode,
            @Size(max = 120) String accountHolderName) {}

    public record GovernmentIds(
            @Size(max = 20) String pan,
            @Size(max = 20) String aadhaar,
            @Size(max = 20) String passport,
            @Size(max = 30) String drivingLicense) {}

    public record Social(
            @Size(max = 200) String linkedinUrl,
            @Size(max = 200) String instagramUrl,
            @Size(max = 200) String facebookUrl,
            @Size(max = 200) String xUrl) {}
}
