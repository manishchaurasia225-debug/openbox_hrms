package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An employment type (e.g. Full-Time, Part-Time, Contract, Intern). Configuration master data
 * managed under the {@code SETTINGS} RBAC module. The unique {@code code} is the stable business key.
 */
@Entity
@Table(name = "employment_types", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employment_types_code", columnNames = "code"),
        @UniqueConstraint(name = "uk_employment_types_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class EmploymentType extends BaseEntity {

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
