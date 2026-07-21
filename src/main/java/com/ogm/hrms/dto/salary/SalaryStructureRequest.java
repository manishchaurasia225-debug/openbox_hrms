package com.ogm.hrms.dto.salary;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A new salary structure revision for an employee. Missing components default to zero. */
public record SalaryStructureRequest(
        @NotNull Long employeeId,
        @NotNull LocalDate effectiveFrom,
        @PositiveOrZero BigDecimal basic,
        @PositiveOrZero BigDecimal hra,
        @PositiveOrZero BigDecimal specialAllowance,
        @PositiveOrZero BigDecimal bonus,
        @PositiveOrZero BigDecimal incentives,
        @PositiveOrZero BigDecimal otherAllowances,
        @Size(max = 300) String remarks
) {
}
