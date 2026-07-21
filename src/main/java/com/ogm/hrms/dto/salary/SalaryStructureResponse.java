package com.ogm.hrms.dto.salary;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Salary structure (revision) view. */
public record SalaryStructureResponse(
        Long id,
        Long employeeId,
        LocalDate effectiveFrom,
        BigDecimal basic,
        BigDecimal hra,
        BigDecimal specialAllowance,
        BigDecimal bonus,
        BigDecimal incentives,
        BigDecimal otherAllowances,
        BigDecimal grossMonthly,
        String remarks
) {
}
