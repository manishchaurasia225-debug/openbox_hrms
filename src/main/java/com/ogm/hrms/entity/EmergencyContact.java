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

/** An employee's emergency contact (child of {@link Employee}). */
@Entity
@Table(name = "employee_emergency_contacts",
        indexes = @Index(name = "idx_emergency_contacts_employee", columnList = "employee_id"))
@Getter
@Setter
@NoArgsConstructor
public class EmergencyContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_emergency_contacts_employee"))
    private Employee employee;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "relationship", length = 60)
    private String relationship;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "alternate_phone", length = 30)
    private String alternatePhone;

    @Column(name = "email", length = 190)
    private String email;

    @Column(name = "address", length = 300)
    private String address;
}
