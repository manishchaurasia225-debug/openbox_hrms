package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.AutomationRunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * An immutable ledger entry recording one execution of an {@link AutomationRule}. The engine uses a
 * prior {@code SUCCESS} row for the same rule and {@code runDate} to stay idempotent across scheduler
 * restarts; manual runs are recorded but bypass that guard.
 */
@Entity
@Table(name = "automation_runs",
        indexes = {
                @Index(name = "idx_automation_runs_rule_date", columnList = "rule_id, run_date"),
                @Index(name = "idx_automation_runs_run_date", columnList = "run_date")
        })
@Getter
@Setter
@NoArgsConstructor
public class AutomationRun extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private AutomationRule rule;

    @Column(name = "run_date", nullable = false)
    private LocalDate runDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AutomationRunStatus status;

    /** Whether this run was triggered manually (vs. by the scheduler). */
    @Column(name = "manual", nullable = false)
    private boolean manual = false;

    /** Number of recipients the rule matched. */
    @Column(name = "matched", nullable = false)
    private int matched;

    /** Number of notifications actually dispatched (matched × configured channels). */
    @Column(name = "dispatched", nullable = false)
    private int dispatched;

    @Column(name = "detail", length = 500)
    private String detail;
}
