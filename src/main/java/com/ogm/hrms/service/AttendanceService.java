package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.attendance.AttendanceCorrectionRequest;
import com.ogm.hrms.dto.attendance.AttendanceResponse;
import com.ogm.hrms.dto.attendance.AttendanceSummaryResponse;
import com.ogm.hrms.dto.attendance.CheckInRequest;
import com.ogm.hrms.security.AuthenticatedUser;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/** Attendance management (RBAC module {@code ATTENDANCE}). Wi-Fi/IP based; no GPS. */
public interface AttendanceService {

    AttendanceResponse checkIn(AuthenticatedUser principal, CheckInRequest request, String ipAddress, String userAgent);

    AttendanceResponse checkOut(AuthenticatedUser principal);

    PageResponse<AttendanceResponse> myHistory(AuthenticatedUser principal, LocalDate from, LocalDate to, Pageable pageable);

    PageResponse<AttendanceResponse> list(Long employeeId, LocalDate date, LocalDate from, LocalDate to, Pageable pageable);

    AttendanceResponse correct(AttendanceCorrectionRequest request);

    AttendanceResponse decideApproval(Long id, boolean approve, String approver);

    AttendanceSummaryResponse monthlySummary(Long employeeId, int year, int month);
}
