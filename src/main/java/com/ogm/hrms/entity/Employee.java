package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.entity.embed.BankDetails;
import com.ogm.hrms.entity.embed.ContactInfo;
import com.ogm.hrms.entity.embed.GovernmentIds;
import com.ogm.hrms.entity.embed.SalaryInfo;
import com.ogm.hrms.entity.embed.SocialProfiles;
import com.ogm.hrms.enums.EmploymentStatus;
import com.ogm.hrms.enums.Gender;
import com.ogm.hrms.enums.MaritalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * The employee master record — the single source of truth for a person employed by the company.
 * Groups scalar attributes into embeddables (contact/salary/bank/government-ids/social) and links to
 * the organization masters (department, designation, employment type) and, optionally, a login
 * {@link User}. Per project-rules.md, removed fields (reporting-manager, work-location,
 * confirmation-date, skills/certificates/github/portfolio, PF/UAN/ESI/tax) are intentionally absent.
 */
@Entity
@Table(name = "employees", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employees_code", columnNames = "employee_code"),
        @UniqueConstraint(name = "uk_employees_user", columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Employee extends BaseEntity {

    // --- Personal ---
    @Column(name = "employee_code", nullable = false, length = 40)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "nationality", length = 60)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Embedded
    private ContactInfo contact = new ContactInfo();

    // --- Employment ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_employees_department"))
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id", foreignKey = @ForeignKey(name = "fk_employees_designation"))
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_type_id", foreignKey = @ForeignKey(name = "fk_employees_employment_type"))
    private EmploymentType employmentType;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    // --- Optional login account ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_employees_user"))
    private User user;

    // --- Compensation / identity (embedded) ---
    @Embedded
    private SalaryInfo salary = new SalaryInfo();

    @Embedded
    private BankDetails bankDetails = new BankDetails();

    @Embedded
    private GovernmentIds governmentIds = new GovernmentIds();

    @Embedded
    private SocialProfiles socialProfiles = new SocialProfiles();
}
