package com.ogm.hrms.dto.dashboard;

import com.ogm.hrms.dto.attendance.AttendanceSummaryResponse;
import com.ogm.hrms.dto.communication.AnnouncementResponse;
import com.ogm.hrms.dto.holiday.HolidayResponse;
import com.ogm.hrms.dto.leave.LeaveBalanceResponse;

import java.util.List;

/** The signed-in employee's self-service dashboard, composed from several modules. */
public record EmployeeDashboardResponse(
        Long employeeId,
        String employeeName,
        String employeeCode,
        String department,
        String designation,
        int profileCompletionPercent,
        AttendanceSummaryResponse attendanceThisMonth,
        List<LeaveBalanceResponse> leaveBalances,
        List<HolidayResponse> upcomingHolidays,
        List<AnnouncementResponse> recentAnnouncements,
        long unreadNotifications,
        long payslipCount
) {
}
