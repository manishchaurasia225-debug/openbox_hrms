package com.ogm.hrms.ai.tool;

import com.ogm.hrms.ai.AiTool;
import com.ogm.hrms.ai.AiToolRequest;
import com.ogm.hrms.ai.AiToolResult;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.attendance.AttendanceResponse;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.service.AttendanceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** AI tool: looks up attendance for a date. Wraps {@link AttendanceService}; requires ATTENDANCE:VIEW. */
@Component
public class AttendanceSearchTool implements AiTool {

    private static final int MAX_RESULTS = 50;

    private final AttendanceService attendanceService;

    public AttendanceSearchTool(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Override
    public String name() {
        return "attendance_search";
    }

    @Override
    public String description() {
        return "Look up attendance records for a date. Parameters: date (yyyy-MM-dd, defaults to today), "
                + "employeeId (optional).";
    }

    @Override
    public String requiredAuthority() {
        return "ATTENDANCE:VIEW";
    }

    @Override
    public AiToolResult execute(AiToolRequest request) {
        LocalDate date = parseDate(request.param("date", null));
        Long employeeId = parseId(request.param("employeeId", null));
        PageResponse<AttendanceResponse> page =
                attendanceService.list(employeeId, date, null, null, PageRequest.of(0, MAX_RESULTS));
        String summary = "Attendance for " + date + ": " + page.totalElements() + " record(s)"
                + (employeeId != null ? " for employee #" + employeeId : "") + ".";
        return new AiToolResult(summary, page.content());
    }

    private LocalDate parseDate(String raw) {
        if (raw == null) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            throw ApiException.badRequest("Invalid date '" + raw + "'; expected yyyy-MM-dd");
        }
    }

    private Long parseId(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw ApiException.badRequest("Invalid employeeId '" + raw + "'");
        }
    }
}
