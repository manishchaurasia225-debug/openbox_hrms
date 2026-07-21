package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.TimelineEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * An append-only event on an employee's timeline (joining, promotion, transfer, notes…). Created by
 * the system on significant changes and manually by HR; not edited after the fact.
 */
@Entity
@Table(name = "employee_timeline_events",
        indexes = @Index(name = "idx_timeline_events_employee", columnList = "employee_id"))
@Getter
@Setter
@NoArgsConstructor
public class EmployeeTimelineEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_timeline_events_employee"))
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private TimelineEventType eventType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;
}
