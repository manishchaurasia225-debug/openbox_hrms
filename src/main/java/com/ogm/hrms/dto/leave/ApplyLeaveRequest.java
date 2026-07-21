package com.ogm.hrms.dto.leave;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** An employee's leave application. {@code halfDay} applies only to a single-day request. */
public record ApplyLeaveRequest(
        @NotNull Long leaveTypeId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        Boolean halfDay,
        @Size(max = 500) String reason
) {
}
