package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.LocationRequest;
import com.ogm.hrms.dto.org.LocationResponse;
import com.ogm.hrms.service.LocationService;
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
 * Office Location configuration API. Reads require {@code SETTINGS:VIEW}; mutations require
 * {@code SETTINGS:ADMIN}.
 */
@Tag(name = "Locations",
        description = "Office location configuration: create, list, retrieve, update, and delete locations.")
@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @Operation(summary = "Create location",
            description = "Creates a new office location. Requires SETTINGS:ADMIN.")
    @PostMapping
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<LocationResponse> create(@Valid @RequestBody LocationRequest request,
                                                HttpServletRequest http) {
        return ApiResponse.success(locationService.create(request), "Location created", http.getRequestURI());
    }

    @Operation(summary = "List locations",
            description = "Returns a paginated list of office locations. Requires SETTINGS:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('SETTINGS:VIEW')")
    public ApiResponse<PageResponse<LocationResponse>> list(@PageableDefault(size = 20) Pageable pageable,
                                                            HttpServletRequest http) {
        return ApiResponse.success(locationService.list(pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get location by ID",
            description = "Retrieves a single office location by its identifier. Requires SETTINGS:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS:VIEW')")
    public ApiResponse<LocationResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(locationService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update location",
            description = "Updates an existing office location. Requires SETTINGS:ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<LocationResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody LocationRequest request,
                                                HttpServletRequest http) {
        return ApiResponse.success(locationService.update(id, request), "Location updated", http.getRequestURI());
    }

    @Operation(summary = "Delete location",
            description = "Deletes an office location. Requires SETTINGS:ADMIN.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SETTINGS:ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        locationService.delete(id);
        return ApiResponse.success(null, "Location deleted", http.getRequestURI());
    }
}
