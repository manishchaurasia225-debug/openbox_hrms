package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.leave.LeaveTypeRequest;
import com.ogm.hrms.dto.leave.LeaveTypeResponse;
import com.ogm.hrms.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Leave-type configuration. Reads require {@code LEAVE:VIEW}; changes require {@code LEAVE:ADMIN}. */
@Tag(name = "Leave Types", description = "Configuration of leave types; reads require LEAVE:VIEW and changes require LEAVE:ADMIN.")
@RestController
@RequestMapping("/api/v1/leave-types")
public class LeaveTypeController {

    private final LeaveService leaveService;

    public LeaveTypeController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @Operation(summary = "List leave types", description = "Returns all configured leave types. Requires LEAVE:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('LEAVE:VIEW')")
    public ApiResponse<List<LeaveTypeResponse>> list(HttpServletRequest http) {
        return ApiResponse.success(leaveService.listTypes(), "OK", http.getRequestURI());
    }

    @Operation(summary = "Create leave type", description = "Creates a new leave type configuration. Requires LEAVE:ADMIN.")
    @PostMapping
    @PreAuthorize("hasAuthority('LEAVE:ADMIN')")
    public ApiResponse<LeaveTypeResponse> create(@Valid @RequestBody LeaveTypeRequest request, HttpServletRequest http) {
        return ApiResponse.success(leaveService.createType(request), "Leave type created", http.getRequestURI());
    }

    @Operation(summary = "Update leave type", description = "Updates an existing leave type configuration by id. Requires LEAVE:ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE:ADMIN')")
    public ApiResponse<LeaveTypeResponse> update(@PathVariable Long id, @Valid @RequestBody LeaveTypeRequest request,
                                                 HttpServletRequest http) {
        return ApiResponse.success(leaveService.updateType(id, request), "Leave type updated", http.getRequestURI());
    }

    @Operation(summary = "Delete leave type", description = "Deletes a leave type configuration by id. Requires LEAVE:ADMIN.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE:ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        leaveService.deleteType(id);
        return ApiResponse.success(null, "Leave type deleted", http.getRequestURI());
    }
}
