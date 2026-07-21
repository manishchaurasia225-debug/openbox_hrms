package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.reimbursement.ReimbursementDecisionRequest;
import com.ogm.hrms.dto.reimbursement.ReimbursementResponse;
import com.ogm.hrms.dto.reimbursement.SubmitReimbursementRequest;
import com.ogm.hrms.enums.ReimbursementStatus;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.ReimbursementService;
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

/**
 * Reimbursement API. Submitting/cancelling is self-service ({@code EXPENSE:CREATE}); approving,
 * rejecting, and paying require {@code EXPENSE:APPROVE}.
 */
@Tag(name = "Reimbursements", description = "Submit, review, approve, reject, pay, and cancel employee expense reimbursement claims.")
@RestController
@RequestMapping("/api/v1/reimbursements")
public class ReimbursementController {

    private final ReimbursementService reimbursementService;

    public ReimbursementController(ReimbursementService reimbursementService) {
        this.reimbursementService = reimbursementService;
    }

    @Operation(summary = "Submit reimbursement claim", description = "Submits a new expense reimbursement claim for the authenticated user. Requires EXPENSE:CREATE.")
    @PostMapping
    @PreAuthorize("hasAuthority('EXPENSE:CREATE')")
    public ApiResponse<ReimbursementResponse> submit(@AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody SubmitReimbursementRequest request, HttpServletRequest http) {
        return ApiResponse.success(reimbursementService.submit(principal, request), "Claim submitted", http.getRequestURI());
    }

    @Operation(summary = "List my claims", description = "Returns a paginated list of the authenticated user's own reimbursement claims. Requires EXPENSE:VIEW.")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('EXPENSE:VIEW')")
    public ApiResponse<PageResponse<ReimbursementResponse>> myClaims(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(reimbursementService.myClaims(principal, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "List reimbursement claims", description = "Returns a paginated list of claims, optionally filtered by employee and status. Requires EXPENSE:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE:VIEW')")
    public ApiResponse<PageResponse<ReimbursementResponse>> list(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) ReimbursementStatus status,
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(reimbursementService.list(employeeId, status, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Approve claim", description = "Approves a reimbursement claim with optional remarks. Requires EXPENSE:APPROVE.")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('EXPENSE:APPROVE')")
    public ApiResponse<ReimbursementResponse> approve(@PathVariable Long id,
            @RequestBody(required = false) ReimbursementDecisionRequest body,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        return ApiResponse.success(reimbursementService.approve(id, principal, remarks(body)), "Decision recorded",
                http.getRequestURI());
    }

    @Operation(summary = "Reject claim", description = "Rejects a reimbursement claim with optional remarks. Requires EXPENSE:APPROVE.")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('EXPENSE:APPROVE')")
    public ApiResponse<ReimbursementResponse> reject(@PathVariable Long id,
            @RequestBody(required = false) ReimbursementDecisionRequest body,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        return ApiResponse.success(reimbursementService.reject(id, principal, remarks(body)), "Claim rejected",
                http.getRequestURI());
    }

    @Operation(summary = "Mark claim paid", description = "Marks an approved reimbursement claim as paid. Requires EXPENSE:APPROVE.")
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('EXPENSE:APPROVE')")
    public ApiResponse<ReimbursementResponse> pay(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        return ApiResponse.success(reimbursementService.pay(id, principal), "Claim paid", http.getRequestURI());
    }

    @Operation(summary = "Cancel claim", description = "Cancels the authenticated user's own pending reimbursement claim. Requires EXPENSE:CREATE.")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('EXPENSE:CREATE')")
    public ApiResponse<ReimbursementResponse> cancel(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal, HttpServletRequest http) {
        return ApiResponse.success(reimbursementService.cancel(id, principal), "Claim cancelled", http.getRequestURI());
    }

    private String remarks(ReimbursementDecisionRequest body) {
        return body != null ? body.remarks() : null;
    }
}
