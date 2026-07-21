package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.entity.converter.NotificationChannelSetConverter;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.enums.NotificationChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A configurable automation: the single row that governs whether, how, and with what wording a given
 * {@link AutomationType} occasion is dispatched. There is exactly one rule per type (seeded at
 * bootstrap). Rules are data, not code — administrators toggle {@code enabled}, choose channels, and
 * edit the message templates; the engine only supplies the matching recipients and variable values.
 */
@Entity
@Table(name = "automation_rules",
        uniqueConstraints = @UniqueConstraint(name = "uk_automation_rules_type", columnNames = "type"))
@Getter
@Setter
@NoArgsConstructor
public class AutomationRule extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private AutomationType type;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /** The channels this rule dispatches over, stored as a comma-separated column. */
    @Convert(converter = NotificationChannelSetConverter.class)
    @Column(name = "channels", nullable = false, length = 200)
    private Set<NotificationChannel> channels = new LinkedHashSet<>();

    /** Notification title template; supports {@code {placeholder}} tokens. */
    @Column(name = "title_template", nullable = false, length = 200)
    private String titleTemplate;

    /** Notification body template; supports {@code {placeholder}} tokens. */
    @Column(name = "message_template", nullable = false, length = 1000)
    private String messageTemplate;

    /**
     * Look-ahead window in days for reminder-style rules (e.g. confirmation and contract-expiry
     * reminders fire this many days before the target date). Null for same-day occasions.
     */
    @Column(name = "lead_days")
    private Integer leadDays;
}
