package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.lifecycle.AddLifecycleTaskRequest;
import com.ogm.hrms.dto.lifecycle.InitiateLifecycleRequest;
import com.ogm.hrms.dto.lifecycle.LifecycleCaseResponse;
import com.ogm.hrms.enums.LifecycleStatus;
import com.ogm.hrms.enums.LifecycleType;
import com.ogm.hrms.service.LifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Employee Lifecycle API (onboarding/offboarding checklists). Initiating requires
 * {@code EMPLOYEE:CREATE}; task/case updates {@code EMPLOYEE:EDIT}; reads {@code EMPLOYEE:VIEW}.
 */
@Tag(name = "Employee Lifecycle", description = "Onboarding and offboarding lifecycle cases and their task checklists, authorized under EMPLOYEE permissions.")
@RestController
@RequestMapping("/api/v1/lifecycle")
public class LifecycleController {

    private final LifecycleService lifecycleService;

    public LifecycleController(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @Operation(summary = "Initiate lifecycle case", description = "Starts a new onboarding or offboarding lifecycle case for an employee. Requires EMPLOYEE:CREATE.")
    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:CREATE')")
    public ApiResponse<LifecycleCaseResponse> initiate(@Valid @RequestBody InitiateLifecycleRequest request,
            HttpServletRequest http) {
        return ApiResponse.success(lifecycleService.initiate(request), "Lifecycle case initiated", http.getRequestURI());
    }

    @Operation(summary = "List lifecycle cases", description = "Returns paginated lifecycle cases filtered by employee, type, and status. Requires EMPLOYEE:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<PageResponse<LifecycleCaseResponse>> list(@RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LifecycleType type,
            @RequestParam(required = false) LifecycleStatus status,
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(lifecycleService.list(employeeId, type, status, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get lifecycle case by id", description = "Returns a single lifecycle case with its tasks by identifier. Requires EMPLOYEE:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<LifecycleCaseResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(lifecycleService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Add lifecycle task", description = "Adds a task to an existing lifecycle case. Requires EMPLOYEE:EDIT.")
    @PostMapping("/{id}/tasks")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<LifecycleCaseResponse> addTask(@PathVariable Long id,
            @Valid @RequestBody AddLifecycleTaskRequest request, HttpServletRequest http) {
        return ApiResponse.success(lifecycleService.addTask(id, request), "Task added", http.getRequestURI());
    }

    @Operation(summary = "Complete lifecycle task", description = "Marks a lifecycle case task as completed with optional notes. Requires EMPLOYEE:EDIT.")
    @PostMapping("/{id}/tasks/{taskId}/complete")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<LifecycleCaseResponse> completeTask(@PathVariable Long id, @PathVariable Long taskId,
            @RequestParam(required = false) String notes, HttpServletRequest http) {
        return ApiResponse.success(lifecycleService.completeTask(id, taskId, notes), "Task completed",
                http.getRequestURI());
    }

    @Operation(summary = "Cancel lifecycle case", description = "Cancels an in-progress lifecycle case. Requires EMPLOYEE:EDIT.")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<LifecycleCaseResponse> cancel(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(lifecycleService.cancel(id), "Lifecycle case cancelled", http.getRequestURI());
    }
}
