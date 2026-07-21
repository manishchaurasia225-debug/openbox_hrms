package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** A prior work-experience record for an employee (child of {@link Employee}). */
@Entity
@Table(name = "employee_experience",
        indexes = @Index(name = "idx_employee_experience_employee", columnList = "employee_id"))
@Getter
@Setter
@NoArgsConstructor
public class EmployeeExperience extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_employee_experience_employee"))
    private Employee employee;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "designation", length = 150)
    private String designation;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "description", length = 500)
    private String description;
}
