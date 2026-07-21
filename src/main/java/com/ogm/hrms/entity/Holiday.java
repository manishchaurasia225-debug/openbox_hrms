package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.HolidayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * A holiday on the company calendar (national, regional, or company). Managed under the
 * {@code HOLIDAY} RBAC module. A given date may have multiple holidays, but not two with the same
 * name.
 */
@Entity
@Table(name = "holidays",
        uniqueConstraints = @UniqueConstraint(name = "uk_holidays_date_name", columnNames = {"holiday_date", "name"}),
        indexes = @Index(name = "idx_holidays_date", columnList = "holiday_date"))
@Getter
@Setter
@NoArgsConstructor
public class Holiday extends BaseEntity {

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private HolidayType type;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "recurring", nullable = false)
    private boolean recurring = false;
}
