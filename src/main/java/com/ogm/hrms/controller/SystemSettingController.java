package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.org.SystemSettingRequest;
import com.ogm.hrms.dto.org.SystemSettingResponse;
import com.ogm.hrms.dto.org.SystemSettingValueRequest;
import com.ogm.hrms.service.SystemSettingService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * System-settings API (key/value config). Reads require {@code SETTINGS:VIEW}; mutations require
 * {@code SETTINGS:ADMIN}. Settings flagged non-editable cannot be changed or removed.
 */
@Tag(name = "System Settings", description = "Key/value configuration store for platform-wide settings.")
@RestController
@RequestMapping("/api/v1/settings")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    public SystemSettingController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @Operation(summary = "List settings",
            description = "Returns all system settings, optionally filtered by category. Requires the SETTINGS:VIEW permission.")
    @GetMapping
    @PreAuthorize("hasAuthority('SETTINGS:VIEW')")
    public ApiResponse<List<SystemSettingResponse>> list(@RequestParam(required = false) String category,
                                                         HttpServletRequest http) {
        return ApiResponse.success(systemSettingService.list(category), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get a setting",
            description = "Returns a single system setting by its key. Requires the SETTINGS:VIEW permission.")
    @GetMapping("/{key}")
    @PreAuthorize("hasAuthority('SETTINGS:VIEW')")
    public ApiResponse<SystemSettingResponse> get(@PathVariable String key, HttpServletRequest http) {
        return ApiResponse.success(systemSettingService.get(key), "OK", http.getRequestURI());
    }

    @Operation(summary = "Create a setting",
            description = "Creates a new system setting entry. Requires the SETTINGS:ADMIN permission.")
    @PostMapping
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<SystemSettingResponse> create(@Valid @RequestBody SystemSettingRequest request,
                                                     HttpServletRequest http) {
        return ApiResponse.success(systemSettingService.create(request), "Setting created", http.getRequestURI());
    }

    @Operation(summary = "Update a setting value",
            description = "Updates the value of an existing setting; non-editable settings are rejected. Requires the SETTINGS:ADMIN permission.")
    @PutMapping("/{key}")
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<SystemSettingResponse> update(@PathVariable String key,
                                                     @Valid @RequestBody SystemSettingValueRequest request,
                                                     HttpServletRequest http) {
        return ApiResponse.success(systemSettingService.updateValue(key, request.value()), "Setting updated",
                http.getRequestURI());
    }

    @Operation(summary = "Delete a setting",
            description = "Removes a system setting by its key; non-editable settings cannot be removed. Requires the SETTINGS:ADMIN permission.")
    @DeleteMapping("/{key}")
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<Void> delete(@PathVariable String key, HttpServletRequest http) {
        systemSettingService.delete(key);
        return ApiResponse.success(null, "Setting deleted", http.getRequestURI());
    }
}
