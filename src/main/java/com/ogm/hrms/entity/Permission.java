package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.PermissionAction;
import com.ogm.hrms.enums.PermissionModule;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single grantable permission — a ({@link PermissionModule}, {@link PermissionAction}) pair
 * identified by its {@code code} (e.g. {@code EMPLOYEE:VIEW}). Permissions are stored as data and
 * assigned to roles dynamically; the {@code code} is used as the Spring Security authority string.
 */
@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(name = "uk_permissions_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "code", callSuper = false)
public class Permission extends BaseEntity {

    @Column(name = "code", nullable = false, length = 80)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "module", nullable = false, length = 40)
    private PermissionModule module;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private PermissionAction action;

    @Column(name = "description", length = 255)
    private String description;

    public Permission(PermissionModule module, PermissionAction action, String description) {
        this.module = module;
        this.action = action;
        this.code = module.name() + ":" + action.name();
        this.description = description;
    }
}
