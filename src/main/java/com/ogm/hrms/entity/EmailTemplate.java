package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.EmailTemplateCategory;
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

/**
 * A reusable email template (Module 19). The subject and HTML body may contain {@code {placeholder}}
 * tokens resolved per send. Templates are addressed by a stable {@code code} so callers (e.g. account
 * flows, automations) can render by code without hardcoding wording.
 */
@Entity
@Table(name = "email_templates",
        uniqueConstraints = @UniqueConstraint(name = "uk_email_templates_code", columnNames = "code"),
        indexes = @Index(name = "idx_email_templates_category", columnList = "category"))
@Getter
@Setter
@NoArgsConstructor
public class EmailTemplate extends BaseEntity {

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private EmailTemplateCategory category;

    /** Subject line template; supports {@code {placeholder}} tokens. */
    @Column(name = "subject", nullable = false, length = 300)
    private String subject;

    /** HTML body template; supports {@code {placeholder}} tokens. */
    @Column(name = "body_html", nullable = false, columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "description", length = 300)
    private String description;
}
