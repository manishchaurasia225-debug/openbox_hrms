package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.search.GlobalSearchResponse;
import com.ogm.hrms.service.GlobalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Global search API: one query, permission-filtered typed results. Any authenticated user may call
 * it; each entity type is only searched if the caller holds that module's VIEW authority.
 */
@Tag(name = "Global Search", description = "Unified, permission-filtered search across HRMS entity types.")
@RestController
@RequestMapping("/api/v1/search")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    public GlobalSearchController(GlobalSearchService globalSearchService) {
        this.globalSearchService = globalSearchService;
    }

    @Operation(summary = "Global search",
            description = "Runs a single query across entity types the caller can view and returns typed results. Requires an authenticated user.")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GlobalSearchResponse> search(@RequestParam("q") String query,
            @RequestParam(name = "limit", defaultValue = "5") int limit, HttpServletRequest http) {
        return ApiResponse.success(globalSearchService.search(query, limit), "OK", http.getRequestURI());
    }
}
