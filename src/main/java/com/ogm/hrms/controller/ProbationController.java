package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.probation.ConfirmProbationRequest;
import com.ogm.hrms.dto.probation.ExtendProbationRequest;
import com.ogm.hrms.dto.probation.ProbationResponse;
import com.ogm.hrms.dto.probation.StartProbationRequest;
import com.ogm.hrms.enums.ProbationStatus;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.ProbationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Confirmation & Probation API (authorized under {@code EMPLOYEE}). Starting/tracking uses
 * {@code EMPLOYEE:EDIT}/{@code EMPLOYEE:VIEW}; confirm/extend/terminate are approvals
 * ({@code EMPLOYEE:APPROVE}).
 */
@Tag(name = "Confirmation & Probation", description = "Employee probation tracking and confirmation, extension, or termination, authorized under EMPLOYEE permissions.")
@RestController
@RequestMapping("/api/v1/probation")
public class ProbationController {

    private final ProbationService probationService;

    public ProbationController(ProbationService probationService) {
        this.probationService = probationService;
    }

    @Operation(summary = "Start probation", description = "Starts a probation period for an employee. Requires EMPLOYEE:EDIT.")
    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<ProbationResponse> start(@Valid @RequestBody StartProbationRequest request, HttpServletRequest http) {
        return ApiResponse.success(probationService.start(request), "Probation started", http.getRequestURI());
    }

    @Operation(summary = "List probations", description = "Returns paginated probation records filtered by employee and status. Requires EMPLOYEE:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<PageResponse<ProbationResponse>> list(@RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) ProbationStatus status,
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(probationService.list(employeeId, status, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "List upcoming probation ends", description = "Returns probations ending within the given number of days (default 30). Requires EMPLOYEE:VIEW.")
    @GetMapping("/upcoming")
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<List<ProbationResponse>> upcoming(@RequestParam(defaultValue = "30") int withinDays,
            HttpServletRequest http) {
        return ApiResponse.success(probationService.upcoming(withinDays), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get probation by id", description = "Returns a single probation record by its identifier. Requires EMPLOYEE:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<ProbationResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(probationService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Confirm employee", description = "Confirms an employee at the end of probation with optional details. Requires EMPLOYEE:APPROVE.")
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('EMPLOYEE:APPROVE')")
    public ApiResponse<ProbationResponse> confirm(@PathVariable Long id,
            @RequestBody(required = false) ConfirmProbationRequest body,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        ConfirmProbationRequest request = body != null ? body : new ConfirmProbationRequest(null, null);
        return ApiResponse.success(probationService.confirm(id, principal, request), "Employee confirmed",
                http.getRequestURI());
    }

    @Operation(summary = "Extend probation", description = "Extends an employee's probation period. Requires EMPLOYEE:APPROVE.")
    @PostMapping("/{id}/extend")
    @PreAuthorize("hasAuthority('EMPLOYEE:APPROVE')")
    public ApiResponse<ProbationResponse> extend(@PathVariable Long id, @Valid @RequestBody ExtendProbationRequest body,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        return ApiResponse.success(probationService.extend(id, principal, body), "Probation extended",
                http.getRequestURI());
    }

    @Operation(summary = "Terminate probation", description = "Terminates an employee during probation with optional remarks. Requires EMPLOYEE:APPROVE.")
    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority('EMPLOYEE:APPROVE')")
    public ApiResponse<ProbationResponse> terminate(@PathVariable Long id,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        return ApiResponse.success(probationService.terminate(id, principal, remarks), "Probation terminated",
                http.getRequestURI());
    }
}
