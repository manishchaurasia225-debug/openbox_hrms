package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A dated salary structure (revision) for an employee. The most recent record whose
 * {@code effectiveFrom} is on/before a date is the salary in effect then; the full set is the
 * revision history. Per project-rules.md there are no statutory deductions (PF/UAN/ESI/tax) — this
 * is earnings only. Exact decimals for money.
 */
@Entity
@Table(name = "salary_structures",
        indexes = @Index(name = "idx_salary_structures_employee", columnList = "employee_id"))
@Getter
@Setter
@NoArgsConstructor
public class SalaryStructure extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_salary_structures_employee"))
    private Employee employee;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "basic", nullable = false, precision = 14, scale = 2)
    private BigDecimal basic = BigDecimal.ZERO;

    @Column(name = "hra", nullable = false, precision = 14, scale = 2)
    private BigDecimal hra = BigDecimal.ZERO;

    @Column(name = "special_allowance", nullable = false, precision = 14, scale = 2)
    private BigDecimal specialAllowance = BigDecimal.ZERO;

    @Column(name = "bonus", nullable = false, precision = 14, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "incentives", nullable = false, precision = 14, scale = 2)
    private BigDecimal incentives = BigDecimal.ZERO;

    @Column(name = "other_allowances", nullable = false, precision = 14, scale = 2)
    private BigDecimal otherAllowances = BigDecimal.ZERO;

    @Column(name = "gross_monthly", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossMonthly = BigDecimal.ZERO;

    @Column(name = "remarks", length = 300)
    private String remarks;
}
