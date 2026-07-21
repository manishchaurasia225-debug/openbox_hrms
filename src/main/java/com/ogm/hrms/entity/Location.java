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
 * An office/work location. Configuration master data managed under the {@code SETTINGS} RBAC module.
 * The unique {@code code} is the stable business key; address fields support later attendance and
 * reporting needs (single-company for now — no tenant column, D-004).
 */
@Entity
@Table(name = "locations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_locations_code", columnNames = "code"),
        @UniqueConstraint(name = "uk_locations_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class Location extends BaseEntity {

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "address_line1", length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
