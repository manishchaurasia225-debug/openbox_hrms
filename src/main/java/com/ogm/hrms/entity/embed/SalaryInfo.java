package com.ogm.hrms.entity.embed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Employee salary components (embedded). Exact decimals — never floating point for money. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class SalaryInfo {

    @Column(name = "basic_salary", precision = 14, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "hra", precision = 14, scale = 2)
    private BigDecimal hra;

    @Column(name = "special_allowance", precision = 14, scale = 2)
    private BigDecimal specialAllowance;

    @Column(name = "bonus", precision = 14, scale = 2)
    private BigDecimal bonus;

    @Column(name = "incentives", precision = 14, scale = 2)
    private BigDecimal incentives;

    @Column(name = "other_allowances", precision = 14, scale = 2)
    private BigDecimal otherAllowances;
}
