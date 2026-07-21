package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.WhatsAppTemplateCategory;
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
 * A reusable WhatsApp message template (Module 20). The body may contain {@code {placeholder}} tokens
 * resolved per send. Addressed by a stable {@code code}; the {@link WhatsAppTemplateCategory} mirrors
 * Meta's template taxonomy.
 */
@Entity
@Table(name = "whatsapp_templates",
        uniqueConstraints = @UniqueConstraint(name = "uk_whatsapp_templates_code", columnNames = "code"),
        indexes = @Index(name = "idx_whatsapp_templates_category", columnList = "category"))
@Getter
@Setter
@NoArgsConstructor
public class WhatsAppTemplate extends BaseEntity {

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private WhatsAppTemplateCategory category;

    /** Message body template; supports {@code {placeholder}} tokens. */
    @Column(name = "body_text", nullable = false, length = 1000)
    private String bodyText;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "description", length = 300)
    private String description;
}
