package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.attendance.AttendanceCorrectionRequest;
import com.ogm.hrms.dto.attendance.AttendanceResponse;
import com.ogm.hrms.dto.attendance.AttendanceSummaryResponse;
import com.ogm.hrms.dto.attendance.CheckInRequest;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Attendance API (Wi-Fi/IP based; no GPS). Self-service check-in/out and history for employees;
 * corrections, approvals, listing, and summaries for HR/managers. Authorized by {@code ATTENDANCE:*}.
 */
@Tag(name = "Attendance", description = "Wi-Fi/IP-based attendance: self-service check-in/out, history, corrections, approvals, and monthly summaries.")
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Operation(summary = "Check in", description = "Records the authenticated employee's check-in, capturing client IP and User-Agent. Requires ATTENDANCE:CREATE.")
    @PostMapping("/check-in")
    @PreAuthorize("hasAuthority('ATTENDANCE:CREATE')")
    public ApiResponse<AttendanceResponse> checkIn(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @Valid @RequestBody CheckInRequest request, HttpServletRequest http) {
        AttendanceResponse response = attendanceService.checkIn(principal, request, clientIp(http),
                http.getHeader("User-Agent"));
        return ApiResponse.success(response, "Checked in", http.getRequestURI());
    }

    @Operation(summary = "Check out", description = "Records the authenticated employee's check-out for the current attendance record. Requires ATTENDANCE:CREATE.")
    @PostMapping("/check-out")
    @PreAuthorize("hasAuthority('ATTENDANCE:CREATE')")
    public ApiResponse<AttendanceResponse> checkOut(@AuthenticationPrincipal AuthenticatedUser principal,
                                                    HttpServletRequest http) {
        return ApiResponse.success(attendanceService.checkOut(principal), "Checked out", http.getRequestURI());
    }

    @Operation(summary = "Get my attendance history", description = "Returns a paginated attendance history for the authenticated employee, optionally filtered by a date range. Requires ATTENDANCE:VIEW.")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ATTENDANCE:VIEW')")
    public ApiResponse<PageResponse<AttendanceResponse>> myHistory(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 31) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(attendanceService.myHistory(principal, from, to, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "List attendance records", description = "Returns paginated attendance records filtered by employee, a single date, or a date range. Requires ATTENDANCE:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('ATTENDANCE:VIEW')")
    public ApiResponse<PageResponse<AttendanceResponse>> list(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 50) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(attendanceService.list(employeeId, date, from, to, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Correct an attendance record", description = "Applies a manual correction to an attendance record. Requires ATTENDANCE:EDIT.")
    @PostMapping("/corrections")
    @PreAuthorize("hasAuthority('ATTENDANCE:EDIT')")
    public ApiResponse<AttendanceResponse> correct(@Valid @RequestBody AttendanceCorrectionRequest request,
                                                   HttpServletRequest http) {
        return ApiResponse.success(attendanceService.correct(request), "Attendance corrected", http.getRequestURI());
    }

    @Operation(summary = "Approve attendance record", description = "Approves a pending attendance record or correction. Requires ATTENDANCE:APPROVE.")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ATTENDANCE:APPROVE')")
    public ApiResponse<AttendanceResponse> approve(@PathVariable Long id,
                                                   @AuthenticationPrincipal AuthenticatedUser principal,
                                                   HttpServletRequest http) {
        return ApiResponse.success(attendanceService.decideApproval(id, true, principal.email()),
                "Approved", http.getRequestURI());
    }

    @Operation(summary = "Reject attendance record", description = "Rejects a pending attendance record or correction. Requires ATTENDANCE:APPROVE.")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ATTENDANCE:APPROVE')")
    public ApiResponse<AttendanceResponse> reject(@PathVariable Long id,
                                                  @AuthenticationPrincipal AuthenticatedUser principal,
                                                  HttpServletRequest http) {
        return ApiResponse.success(attendanceService.decideApproval(id, false, principal.email()),
                "Rejected", http.getRequestURI());
    }

    @Operation(summary = "Get monthly attendance summary", description = "Returns an attendance summary for an employee for the given year and month. Requires ATTENDANCE:VIEW.")
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ATTENDANCE:VIEW')")
    public ApiResponse<AttendanceSummaryResponse> summary(@RequestParam Long employeeId,
                                                          @RequestParam int year, @RequestParam int month,
                                                          HttpServletRequest http) {
        return ApiResponse.success(attendanceService.monthlySummary(employeeId, year, month), "OK", http.getRequestURI());
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
