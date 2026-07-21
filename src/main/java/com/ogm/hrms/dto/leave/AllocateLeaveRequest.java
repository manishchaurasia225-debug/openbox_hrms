package com.ogm.hrms.dto.leave;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** Allocate/adjust an employee's leave balance for a type and year. */
public record AllocateLeaveRequest(
        @NotNull Long employeeId,
        @NotNull Long leaveTypeId,
        @NotNull Integer year,
        @NotNull @PositiveOrZero BigDecimal allocated
) {
}
