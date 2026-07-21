package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.DepartmentRequest;
import com.ogm.hrms.dto.org.DepartmentResponse;
import com.ogm.hrms.service.DepartmentService;
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

/** Department master API, authorized by the {@code DEPARTMENT} RBAC permissions. */
@Tag(name = "Departments", description = "Department master records: create, list, retrieve, update, and delete departments.")
@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "Create department",
            description = "Creates a new department master record. Requires DEPARTMENT:CREATE.")
    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT:CREATE')")
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request,
                                                  HttpServletRequest http) {
        return ApiResponse.success(departmentService.create(request), "Department created", http.getRequestURI());
    }

    @Operation(summary = "List departments",
            description = "Returns a paginated list of departments. Requires DEPARTMENT:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('DEPARTMENT:VIEW')")
    public ApiResponse<PageResponse<DepartmentResponse>> list(@PageableDefault(size = 20) Pageable pageable,
                                                              HttpServletRequest http) {
        return ApiResponse.success(departmentService.list(pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get department by ID",
            description = "Retrieves a single department by its identifier. Requires DEPARTMENT:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:VIEW')")
    public ApiResponse<DepartmentResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(departmentService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update department",
            description = "Updates an existing department master record. Requires DEPARTMENT:EDIT.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:EDIT')")
    public ApiResponse<DepartmentResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody DepartmentRequest request,
                                                  HttpServletRequest http) {
        return ApiResponse.success(departmentService.update(id, request), "Department updated", http.getRequestURI());
    }

    @Operation(summary = "Delete department",
            description = "Deletes a department master record. Requires DEPARTMENT:DELETE.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        departmentService.delete(id);
        return ApiResponse.success(null, "Department deleted", http.getRequestURI());
    }
}
