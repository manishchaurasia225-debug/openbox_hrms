package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.PayslipStatus;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * A generated monthly payslip — an immutable snapshot of the salary components for one pay period
 * (at most one per employee per month). The rendered PDF lives in the storage backend
 * ({@code storageKey}); net equals gross since there are no statutory deductions (project-rules.md).
 */
@Entity
@Table(name = "payslips",
        uniqueConstraints = @UniqueConstraint(name = "uk_payslips_emp_period", columnNames = {"employee_id", "period_year", "period_month"}),
        indexes = @Index(name = "idx_payslips_employee", columnList = "employee_id"))
@Getter
@Setter
@NoArgsConstructor
public class Payslip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payslips_employee"))
    private Employee employee;

    @Column(name = "period_year", nullable = false)
    private int periodYear;

    @Column(name = "period_month", nullable = false)
    private int periodMonth;

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

    @Column(name = "gross_pay", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossPay = BigDecimal.ZERO;

    @Column(name = "net_pay", nullable = false, precision = 14, scale = 2)
    private BigDecimal netPay = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayslipStatus status = PayslipStatus.GENERATED;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    @Column(name = "storage_key", length = 400)
    private String storageKey;
}
