package com.ogm.hrms.dto.attendance;

import com.ogm.hrms.enums.ApprovalStatus;
import com.ogm.hrms.enums.AttendanceSource;
import com.ogm.hrms.enums.AttendanceType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Attendance record view. */
public record AttendanceResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate attendanceDate,
        AttendanceType attendanceType,
        AttendanceSource source,
        OffsetDateTime clockIn,
        OffsetDateTime clockOut,
        Integer workingMinutes,
        boolean late,
        boolean halfDay,
        String ipAddress,
        String wfhReason,
        String workLocation,
        ApprovalStatus approvalStatus,
        String remarks
) {
}
