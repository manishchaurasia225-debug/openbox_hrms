package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.org.CompanyProfileRequest;
import com.ogm.hrms.dto.org.CompanyProfileResponse;
import com.ogm.hrms.service.CompanyProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Company profile API (single company). Read requires {@code COMPANY:VIEW}; update {@code COMPANY:EDIT}. */
@Tag(name = "Company", description = "Single-company profile: retrieve and update the company profile.")
@RestController
@RequestMapping("/api/v1/company")
public class CompanyController {

    private final CompanyProfileService companyProfileService;

    public CompanyController(CompanyProfileService companyProfileService) {
        this.companyProfileService = companyProfileService;
    }

    @Operation(summary = "Get company profile",
            description = "Retrieves the single company profile. Requires COMPANY:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('COMPANY:VIEW')")
    public ApiResponse<CompanyProfileResponse> get(HttpServletRequest http) {
        return ApiResponse.success(companyProfileService.get(), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update company profile",
            description = "Updates the single company profile. Requires COMPANY:EDIT.")
    @PutMapping
    @PreAuthorize("hasAuthority('COMPANY:EDIT')")
    public ApiResponse<CompanyProfileResponse> update(@Valid @RequestBody CompanyProfileRequest request,
                                                      HttpServletRequest http) {
        return ApiResponse.success(companyProfileService.update(request), "Company profile updated",
                http.getRequestURI());
    }
}
