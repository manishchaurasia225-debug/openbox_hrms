package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.document.DocumentDownload;
import com.ogm.hrms.dto.salary.GeneratePayslipRequest;
import com.ogm.hrms.dto.salary.PayslipResponse;
import com.ogm.hrms.dto.salary.SalaryStructureRequest;
import com.ogm.hrms.dto.salary.SalaryStructureResponse;
import com.ogm.hrms.service.SalaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Salary & Compensation API. Structure revisions and payslip generation require
 * {@code PAYROLL:CREATE}; reads/downloads require {@code PAYROLL:VIEW}.
 */
@Tag(name = "Salary & Compensation", description = "Manage employee salary structure revisions and generate, list, and download payslips.")
@RestController
@RequestMapping("/api/v1/salary")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @Operation(summary = "Add salary revision", description = "Creates a new salary structure revision for an employee. Requires PAYROLL:CREATE.")
    @PostMapping("/structures")
    @PreAuthorize("hasAuthority('PAYROLL:CREATE')")
    public ApiResponse<SalaryStructureResponse> addRevision(@Valid @RequestBody SalaryStructureRequest request,
                                                            HttpServletRequest http) {
        return ApiResponse.success(salaryService.addRevision(request), "Salary revision added", http.getRequestURI());
    }

    @Operation(summary = "List salary structure history", description = "Returns the full salary structure revision history for an employee. Requires PAYROLL:VIEW.")
    @GetMapping("/structures")
    @PreAuthorize("hasAuthority('PAYROLL:VIEW')")
    public ApiResponse<List<SalaryStructureResponse>> history(@RequestParam Long employeeId, HttpServletRequest http) {
        return ApiResponse.success(salaryService.history(employeeId), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get current salary structure", description = "Returns the currently effective salary structure for an employee. Requires PAYROLL:VIEW.")
    @GetMapping("/structures/current")
    @PreAuthorize("hasAuthority('PAYROLL:VIEW')")
    public ApiResponse<SalaryStructureResponse> current(@RequestParam Long employeeId, HttpServletRequest http) {
        return ApiResponse.success(salaryService.current(employeeId), "OK", http.getRequestURI());
    }

    @Operation(summary = "Generate payslip", description = "Generates a payslip for an employee for the requested pay period. Requires PAYROLL:CREATE.")
    @PostMapping("/payslips")
    @PreAuthorize("hasAuthority('PAYROLL:CREATE')")
    public ApiResponse<PayslipResponse> generate(@Valid @RequestBody GeneratePayslipRequest request,
                                                 HttpServletRequest http) {
        return ApiResponse.success(salaryService.generatePayslip(request), "Payslip generated", http.getRequestURI());
    }

    @Operation(summary = "List payslips", description = "Returns a paginated list of payslips for an employee. Requires PAYROLL:VIEW.")
    @GetMapping("/payslips")
    @PreAuthorize("hasAuthority('PAYROLL:VIEW')")
    public ApiResponse<PageResponse<PayslipResponse>> listPayslips(@RequestParam Long employeeId,
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(salaryService.listPayslips(employeeId, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get payslip by id", description = "Returns a single payslip by its identifier. Requires PAYROLL:VIEW.")
    @GetMapping("/payslips/{id}")
    @PreAuthorize("hasAuthority('PAYROLL:VIEW')")
    public ApiResponse<PayslipResponse> getPayslip(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(salaryService.getPayslip(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Download payslip PDF", description = "Streams the payslip as a downloadable PDF attachment. Requires PAYROLL:VIEW.")
    @GetMapping("/payslips/{id}/download")
    @PreAuthorize("hasAuthority('PAYROLL:VIEW')")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentDownload download = salaryService.downloadPayslip(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.filename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(download.resource());
    }
}
