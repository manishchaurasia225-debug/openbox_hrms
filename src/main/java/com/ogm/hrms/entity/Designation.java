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
 * A job designation / title (e.g. Software Engineer, HR Manager). Master data managed under the
 * {@code DESIGNATION} RBAC module. The unique {@code code} is the stable business key.
 */
@Entity
@Table(name = "designations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_designations_code", columnNames = "code"),
        @UniqueConstraint(name = "uk_designations_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class Designation extends BaseEntity {

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
