package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.EmploymentTypeRequest;
import com.ogm.hrms.dto.org.EmploymentTypeResponse;
import com.ogm.hrms.service.EmploymentTypeService;
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

/**
 * Employment Type configuration API. Reads require {@code SETTINGS:VIEW}; mutations require
 * {@code SETTINGS:ADMIN} (administrative configuration is restricted to admin roles).
 */
@Tag(name = "Employment Types",
        description = "Employment type configuration: create, list, retrieve, update, and delete employment types.")
@RestController
@RequestMapping("/api/v1/employment-types")
public class EmploymentTypeController {

    private final EmploymentTypeService employmentTypeService;

    public EmploymentTypeController(EmploymentTypeService employmentTypeService) {
        this.employmentTypeService = employmentTypeService;
    }

    @Operation(summary = "Create employment type",
            description = "Creates a new employment type configuration. Requires SETTINGS:ADMIN.")
    @PostMapping
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<EmploymentTypeResponse> create(@Valid @RequestBody EmploymentTypeRequest request,
                                                      HttpServletRequest http) {
        return ApiResponse.success(employmentTypeService.create(request), "Employment type created",
                http.getRequestURI());
    }

    @Operation(summary = "List employment types",
            description = "Returns a paginated list of employment types. Requires SETTINGS:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('SETTINGS:VIEW')")
    public ApiResponse<PageResponse<EmploymentTypeResponse>> list(@PageableDefault(size = 20) Pageable pageable,
                                                                  HttpServletRequest http) {
        return ApiResponse.success(employmentTypeService.list(pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get employment type by ID",
            description = "Retrieves a single employment type by its identifier. Requires SETTINGS:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS:VIEW')")
    public ApiResponse<EmploymentTypeResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(employmentTypeService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update employment type",
            description = "Updates an existing employment type configuration. Requires SETTINGS:ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<EmploymentTypeResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody EmploymentTypeRequest request,
                                                      HttpServletRequest http) {
        return ApiResponse.success(employmentTypeService.update(id, request), "Employment type updated",
                http.getRequestURI());
    }

    @Operation(summary = "Delete employment type",
            description = "Deletes an employment type configuration. Requires SETTINGS:ADMIN.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        employmentTypeService.delete(id);
        return ApiResponse.success(null, "Employment type deleted", http.getRequestURI());
    }
}
