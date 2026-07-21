package com.ogm.hrms.dto.attendance;

import com.ogm.hrms.enums.AttendanceType;

import java.util.Map;

/** Monthly attendance summary for an employee. */
public record AttendanceSummaryResponse(
        Long employeeId,
        int year,
        int month,
        long presentDays,
        long leaveDays,
        long absentDays,
        long totalDays,
        long totalWorkingMinutes,
        Map<AttendanceType, Long> countsByType
) {
}
