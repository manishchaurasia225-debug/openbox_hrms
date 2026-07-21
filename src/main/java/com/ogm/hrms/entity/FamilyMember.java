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

/** A member of an employee's family (child of {@link Employee}). */
@Entity
@Table(name = "employee_family_members",
        indexes = @Index(name = "idx_family_members_employee", columnList = "employee_id"))
@Getter
@Setter
@NoArgsConstructor
public class FamilyMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_family_members_employee"))
    private Employee employee;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "relationship", length = 60)
    private String relationship;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "occupation", length = 120)
    private String occupation;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "dependent", nullable = false)
    private boolean dependent = false;
}
