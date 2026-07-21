package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.employee.EmployeeRequest;
import com.ogm.hrms.dto.employee.EmployeeResponse;
import com.ogm.hrms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Employee master API, authorized by the {@code EMPLOYEE} RBAC permissions. */
@Tag(name = "Employees", description = "Employee master records: create, list, retrieve, update, and delete employees.")
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Create employee",
            description = "Creates a new employee master record. Requires EMPLOYEE:CREATE.")
    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:CREATE')")
    public ApiResponse<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request, HttpServletRequest http) {
        return ApiResponse.success(employeeService.create(request), "Employee created", http.getRequestURI());
    }

    @Operation(summary = "List employees",
            description = "Returns a paginated list of employees. Requires EMPLOYEE:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<PageResponse<EmployeeResponse>> list(@PageableDefault(size = 20) Pageable pageable,
                                                            HttpServletRequest http) {
        return ApiResponse.success(employeeService.list(pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get employee by ID",
            description = "Retrieves a single employee by its identifier. Requires EMPLOYEE:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<EmployeeResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(employeeService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update employee",
            description = "Updates an existing employee master record. Requires EMPLOYEE:EDIT.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<EmployeeResponse> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request,
                                                HttpServletRequest http) {
        return ApiResponse.success(employeeService.update(id, request), "Employee updated", http.getRequestURI());
    }

    @Operation(summary = "Delete employee",
            description = "Deletes an employee master record. Requires EMPLOYEE:DELETE.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        employeeService.delete(id);
        return ApiResponse.success(null, "Employee deleted", http.getRequestURI());
    }
}
