package com.ogm.hrms.dto.salary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request to generate a payslip for an employee for a given month/year. */
public record GeneratePayslipRequest(
        @NotNull Long employeeId,
        @NotNull Integer year,
        @NotNull @Min(1) @Max(12) Integer month
) {
}
