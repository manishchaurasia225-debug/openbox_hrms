package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.holiday.HolidayRequest;
import com.ogm.hrms.dto.holiday.HolidayResponse;
import com.ogm.hrms.enums.HolidayType;
import com.ogm.hrms.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

/** Holiday calendar API, authorized by the {@code HOLIDAY} RBAC permissions. */
@Tag(name = "Holidays", description = "Holiday calendar management, authorized by the HOLIDAY RBAC permissions.")
@RestController
@RequestMapping("/api/v1/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @Operation(summary = "Create holiday", description = "Adds a new holiday to the calendar. Requires HOLIDAY:CREATE.")
    @PostMapping
    @PreAuthorize("hasAuthority('HOLIDAY:CREATE')")
    public ApiResponse<HolidayResponse> create(@Valid @RequestBody HolidayRequest request, HttpServletRequest http) {
        return ApiResponse.success(holidayService.create(request), "Holiday created", http.getRequestURI());
    }

    @Operation(summary = "Get holiday calendar", description = "Returns holidays filtered by year, date range, and/or holiday type. Requires HOLIDAY:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('HOLIDAY:VIEW')")
    public ApiResponse<List<HolidayResponse>> calendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) HolidayType type, HttpServletRequest http) {
        return ApiResponse.success(holidayService.calendar(year, from, to, type), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get holiday by id", description = "Returns a single holiday by its identifier. Requires HOLIDAY:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HOLIDAY:VIEW')")
    public ApiResponse<HolidayResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(holidayService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update holiday", description = "Updates an existing holiday by id. Requires HOLIDAY:EDIT.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HOLIDAY:EDIT')")
    public ApiResponse<HolidayResponse> update(@PathVariable Long id, @Valid @RequestBody HolidayRequest request,
                                               HttpServletRequest http) {
        return ApiResponse.success(holidayService.update(id, request), "Holiday updated", http.getRequestURI());
    }

    @Operation(summary = "Delete holiday", description = "Removes a holiday from the calendar by id. Requires HOLIDAY:DELETE.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HOLIDAY:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        holidayService.delete(id);
        return ApiResponse.success(null, "Holiday deleted", http.getRequestURI());
    }
}
