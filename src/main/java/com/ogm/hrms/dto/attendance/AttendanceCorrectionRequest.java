package com.ogm.hrms.dto.attendance;

import com.ogm.hrms.enums.AttendanceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** HR correction/regularization of an employee's attendance for a specific date (upsert). */
public record AttendanceCorrectionRequest(
        @NotNull Long employeeId,
        @NotNull LocalDate date,
        @NotNull AttendanceType attendanceType,
        OffsetDateTime clockIn,
        OffsetDateTime clockOut,
        @Size(max = 300) String remarks
) {
}
