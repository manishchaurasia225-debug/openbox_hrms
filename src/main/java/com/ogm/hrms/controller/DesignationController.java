package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.DesignationRequest;
import com.ogm.hrms.dto.org.DesignationResponse;
import com.ogm.hrms.service.DesignationService;
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

/** Designation master API, authorized by the {@code DESIGNATION} RBAC permissions. */
@Tag(name = "Designations", description = "Designation master records: create, list, retrieve, update, and delete designations.")
@RestController
@RequestMapping("/api/v1/designations")
public class DesignationController {

    private final DesignationService designationService;

    public DesignationController(DesignationService designationService) {
        this.designationService = designationService;
    }

    @Operation(summary = "Create designation",
            description = "Creates a new designation master record. Requires DESIGNATION:CREATE.")
    @PostMapping
    @PreAuthorize("hasAuthority('DESIGNATION:CREATE')")
    public ApiResponse<DesignationResponse> create(@Valid @RequestBody DesignationRequest request,
                                                   HttpServletRequest http) {
        return ApiResponse.success(designationService.create(request), "Designation created", http.getRequestURI());
    }

    @Operation(summary = "List designations",
            description = "Returns a paginated list of designations. Requires DESIGNATION:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('DESIGNATION:VIEW')")
    public ApiResponse<PageResponse<DesignationResponse>> list(@PageableDefault(size = 20) Pageable pageable,
                                                               HttpServletRequest http) {
        return ApiResponse.success(designationService.list(pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get designation by ID",
            description = "Retrieves a single designation by its identifier. Requires DESIGNATION:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DESIGNATION:VIEW')")
    public ApiResponse<DesignationResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(designationService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update designation",
            description = "Updates an existing designation master record. Requires DESIGNATION:EDIT.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DESIGNATION:EDIT')")
    public ApiResponse<DesignationResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody DesignationRequest request,
                                                   HttpServletRequest http) {
        return ApiResponse.success(designationService.update(id, request), "Designation updated", http.getRequestURI());
    }

    @Operation(summary = "Delete designation",
            description = "Deletes a designation master record. Requires DESIGNATION:DELETE.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DESIGNATION:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        designationService.delete(id);
        return ApiResponse.success(null, "Designation deleted", http.getRequestURI());
    }
}
