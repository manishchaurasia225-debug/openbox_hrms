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
 * A configurable leave type (Casual, Sick, Earned, LOP, WFH, Maternity, Paternity, …). Managed under
 * the {@code LEAVE} RBAC module. {@code paid=false} or {@code defaultAnnualQuota=0} types are not
 * balance-tracked (e.g. LOP). Never hardcoded (project-rules.md) — seeded but editable.
 */
@Entity
@Table(name = "leave_types", uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_types_code", columnNames = "code"),
        @UniqueConstraint(name = "uk_leave_types_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class LeaveType extends BaseEntity {

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "default_annual_quota", nullable = false)
    private int defaultAnnualQuota = 0;

    @Column(name = "paid", nullable = false)
    private boolean paid = true;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
