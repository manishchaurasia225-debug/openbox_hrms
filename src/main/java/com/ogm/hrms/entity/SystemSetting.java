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
 * A key/value system setting (configuration store). Managed under the {@code SETTINGS} RBAC module.
 * Holds tunable platform configuration — including attendance policies (working hours, WFH rules,
 * office IP allow-list) — that feature modules read at runtime rather than hardcoding.
 */
@Entity
@Table(name = "system_settings",
        uniqueConstraints = @UniqueConstraint(name = "uk_system_settings_key", columnNames = "setting_key"))
@Getter
@Setter
@NoArgsConstructor
public class SystemSetting extends BaseEntity {

    @Column(name = "setting_key", nullable = false, length = 120)
    private String settingKey;

    @Column(name = "setting_value", length = 2000)
    private String settingValue;

    @Column(name = "category", length = 80)
    private String category;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "editable", nullable = false)
    private boolean editable = true;
}
