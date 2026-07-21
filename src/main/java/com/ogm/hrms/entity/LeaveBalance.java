package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * An employee's leave balance for a given type and year. {@code allocated - used} is the remaining
 * balance, which is never allowed to go negative (business rule). Days use exact decimals to support
 * half-day leave.
 */
@Entity
@Table(name = "leave_balances", uniqueConstraints = @UniqueConstraint(
        name = "uk_leave_balance_emp_type_year", columnNames = {"employee_id", "leave_type_id", "year"}))
@Getter
@Setter
@NoArgsConstructor
public class LeaveBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_leave_balance_employee"))
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_leave_balance_type"))
    private LeaveType leaveType;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "allocated", nullable = false, precision = 6, scale = 1)
    private BigDecimal allocated = BigDecimal.ZERO;

    @Column(name = "used", nullable = false, precision = 6, scale = 1)
    private BigDecimal used = BigDecimal.ZERO;

    public BigDecimal remaining() {
        return allocated.subtract(used);
    }
}
