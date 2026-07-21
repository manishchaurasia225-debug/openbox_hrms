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

/** An education record for an employee (child of {@link Employee}). */
@Entity
@Table(name = "employee_education",
        indexes = @Index(name = "idx_employee_education_employee", columnList = "employee_id"))
@Getter
@Setter
@NoArgsConstructor
public class EmployeeEducation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_employee_education_employee"))
    private Employee employee;

    @Column(name = "institution", nullable = false, length = 200)
    private String institution;

    @Column(name = "degree", length = 150)
    private String degree;

    @Column(name = "field_of_study", length = 150)
    private String fieldOfStudy;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(name = "grade", length = 60)
    private String grade;
}
