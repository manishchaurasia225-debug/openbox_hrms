package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.ProbationStatus;
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
import java.time.OffsetDateTime;

/**
 * Tracks an employee's probation period and confirmation. Modeled as its own entity (the
 * confirmation-date field was removed from the employee master per project-rules.md). Confirmation
 * and extension are HR-approved decisions.
 */
@Entity
@Table(name = "probation_records", indexes = {
        @Index(name = "idx_probation_employee", columnList = "employee_id"),
        @Index(name = "idx_probation_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class ProbationRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_probation_employee"))
    private Employee employee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProbationStatus status = ProbationStatus.IN_PROBATION;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "decided_by", length = 190)
    private String decidedBy;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
