package com.ogm.hrms.dto.employee;

import com.ogm.hrms.enums.EmploymentStatus;
import com.ogm.hrms.enums.Gender;
import com.ogm.hrms.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Employee master view returned by the API. Employment carries resolved master names for display. */
public record EmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        Gender gender,
        LocalDate dateOfBirth,
        String bloodGroup,
        String nationality,
        MaritalStatus maritalStatus,
        String photoUrl,
        Contact contact,
        Employment employment,
        Salary salary,
        Bank bank,
        GovernmentIds governmentIds,
        Social social,
        Long userId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public record Contact(String mobile, String personalEmail, String officialEmail,
                          String currentAddress, String permanentAddress) {}

    public record Employment(Long departmentId, String departmentName,
                             Long designationId, String designationName,
                             Long employmentTypeId, String employmentTypeName,
                             LocalDate joiningDate, LocalDate endDate, Integer noticePeriodDays,
                             EmploymentStatus employmentStatus) {}

    public record Salary(BigDecimal basicSalary, BigDecimal hra, BigDecimal specialAllowance,
                         BigDecimal bonus, BigDecimal incentives, BigDecimal otherAllowances) {}

    public record Bank(String bankName, String accountNumber, String ifscCode, String accountHolderName) {}

    public record GovernmentIds(String pan, String aadhaar, String passport, String drivingLicense) {}

    public record Social(String linkedinUrl, String instagramUrl, String facebookUrl, String xUrl) {}
}
