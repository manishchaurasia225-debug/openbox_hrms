package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.leave.AllocateLeaveRequest;
import com.ogm.hrms.dto.leave.ApplyLeaveRequest;
import com.ogm.hrms.dto.leave.LeaveBalanceResponse;
import com.ogm.hrms.dto.leave.LeaveDecisionRequest;
import com.ogm.hrms.dto.leave.LeaveRequestResponse;
import com.ogm.hrms.enums.LeaveStatus;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.LeaveService;
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
import java.util.List;

/**
 * Leave balances and the request workflow (apply → manager → HR). Applying/cancelling is
 * self-service; approving/rejecting requires {@code LEAVE:APPROVE}; allocation requires
 * {@code LEAVE:EDIT}.
 */
@Tag(name = "Leave", description = "Leave balances and the apply/approve/reject/cancel request workflow, plus allocation and calendar views.")
@RestController
@RequestMapping("/api/v1/leave")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @Operation(summary = "Allocate leave balance", description = "Allocates or adjusts a leave balance for an employee. Requires LEAVE:EDIT.")
    @PostMapping("/allocate")
    @PreAuthorize("hasAuthority('LEAVE:EDIT')")
    public ApiResponse<LeaveBalanceResponse> allocate(@Valid @RequestBody AllocateLeaveRequest request,
                                                      HttpServletRequest http) {
        return ApiResponse.success(leaveService.allocate(request), "Leave allocated", http.getRequestURI());
    }

    @Operation(summary = "Get leave balances", description = "Returns all leave-type balances for an employee for the given year. Requires LEAVE:VIEW.")
    @GetMapping("/balances")
    @PreAuthorize("hasAuthority('LEAVE:VIEW')")
    public ApiResponse<List<LeaveBalanceResponse>> balances(@RequestParam Long employeeId, @RequestParam int year,
                                                            HttpServletRequest http) {
        return ApiResponse.success(leaveService.balances(employeeId, year), "OK", http.getRequestURI());
    }

    @Operation(summary = "Apply for leave", description = "Submits a new leave request for the authenticated employee, starting the approval workflow. Requires LEAVE:CREATE.")
    @PostMapping("/requests")
    @PreAuthorize("hasAuthority('LEAVE:CREATE')")
    public ApiResponse<LeaveRequestResponse> apply(@AuthenticationPrincipal AuthenticatedUser principal,
                                                   @Valid @RequestBody ApplyLeaveRequest request, HttpServletRequest http) {
        return ApiResponse.success(leaveService.apply(principal, request), "Leave requested", http.getRequestURI());
    }

    @Operation(summary = "Get my leave requests", description = "Returns a paginated list of the authenticated employee's leave requests. Requires LEAVE:VIEW.")
    @GetMapping("/requests/me")
    @PreAuthorize("hasAuthority('LEAVE:VIEW')")
    public ApiResponse<PageResponse<LeaveRequestResponse>> myRequests(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(leaveService.myRequests(principal, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "List leave requests", description = "Returns paginated leave requests filtered by employee and status. Requires LEAVE:VIEW.")
    @GetMapping("/requests")
    @PreAuthorize("hasAuthority('LEAVE:VIEW')")
    public ApiResponse<PageResponse<LeaveRequestResponse>> list(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveStatus status,
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(leaveService.list(employeeId, status, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Approve leave request", description = "Approves a leave request with optional remarks, advancing the approval workflow. Requires LEAVE:APPROVE.")
    @PostMapping("/requests/{id}/approve")
    @PreAuthorize("hasAuthority('LEAVE:APPROVE')")
    public ApiResponse<LeaveRequestResponse> approve(@PathVariable Long id,
            @RequestBody(required = false) LeaveDecisionRequest body,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        return ApiResponse.success(leaveService.approve(id, principal, remarks(body)), "Decision recorded",
                http.getRequestURI());
    }

    @Operation(summary = "Reject leave request", description = "Rejects a leave request with optional remarks. Requires LEAVE:APPROVE.")
    @PostMapping("/requests/{id}/reject")
    @PreAuthorize("hasAuthority('LEAVE:APPROVE')")
    public ApiResponse<LeaveRequestResponse> reject(@PathVariable Long id,
            @RequestBody(required = false) LeaveDecisionRequest body,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        return ApiResponse.success(leaveService.reject(id, principal, remarks(body)), "Leave rejected",
                http.getRequestURI());
    }

    @Operation(summary = "Cancel leave request", description = "Cancels the authenticated employee's own leave request. Requires LEAVE:CREATE.")
    @PostMapping("/requests/{id}/cancel")
    @PreAuthorize("hasAuthority('LEAVE:CREATE')")
    public ApiResponse<LeaveRequestResponse> cancel(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        return ApiResponse.success(leaveService.cancel(id, principal), "Leave cancelled", http.getRequestURI());
    }

    @Operation(summary = "Get leave calendar", description = "Returns leave requests overlapping the given date range for a calendar view. Requires LEAVE:VIEW.")
    @GetMapping("/calendar")
    @PreAuthorize("hasAuthority('LEAVE:VIEW')")
    public ApiResponse<List<LeaveRequestResponse>> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, HttpServletRequest http) {
        return ApiResponse.success(leaveService.calendar(from, to), "OK", http.getRequestURI());
    }

    private String remarks(LeaveDecisionRequest body) {
        return body != null ? body.remarks() : null;
    }
}
