package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.automation.AutomationRuleResponse;
import com.ogm.hrms.dto.automation.AutomationRunResponse;
import com.ogm.hrms.dto.automation.UpdateAutomationRuleRequest;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.service.AutomationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Automation Engine administration API. Reads require {@code NOTIFICATION:VIEW}; configuration changes
 * and manual triggers require {@code NOTIFICATION:ADMIN} (decision D-008 — automation rules are the
 * "company notification configuration" the matrix places under the NOTIFICATION admin permission).
 */
@Tag(name = "Automation Engine", description = "Configure scheduled automation rules and inspect or trigger their runs.")
@RestController
@RequestMapping("/api/v1/automations")
public class AutomationController {

    private final AutomationService automationService;

    public AutomationController(AutomationService automationService) {
        this.automationService = automationService;
    }

    @Operation(summary = "List automation rules",
            description = "Returns all configured automation rules. Requires NOTIFICATION:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:VIEW')")
    public ApiResponse<List<AutomationRuleResponse>> listRules(HttpServletRequest http) {
        return ApiResponse.success(automationService.listRules(), "OK", http.getRequestURI());
    }

    @Operation(summary = "List automation runs",
            description = "Returns a paginated history of automation rule executions. Requires NOTIFICATION:VIEW.")
    @GetMapping("/runs")
    @PreAuthorize("hasAuthority('NOTIFICATION:VIEW')")
    public ApiResponse<PageResponse<AutomationRunResponse>> listRuns(
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(automationService.listRuns(pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get automation rule",
            description = "Returns the automation rule for the given automation type. Requires NOTIFICATION:VIEW.")
    @GetMapping("/{type}")
    @PreAuthorize("hasAuthority('NOTIFICATION:VIEW')")
    public ApiResponse<AutomationRuleResponse> getRule(@PathVariable AutomationType type, HttpServletRequest http) {
        return ApiResponse.success(automationService.getRule(type), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update automation rule",
            description = "Updates configuration (schedule, enablement) for an automation rule. Requires NOTIFICATION:ADMIN.")
    @PatchMapping("/{type}")
    @PreAuthorize("hasAuthority('NOTIFICATION:ADMIN')")
    public ApiResponse<AutomationRuleResponse> updateRule(@PathVariable AutomationType type,
            @Valid @RequestBody UpdateAutomationRuleRequest request, HttpServletRequest http) {
        return ApiResponse.success(automationService.updateRule(type, request), "Automation rule updated",
                http.getRequestURI());
    }

    @Operation(summary = "Run automation now",
            description = "Manually triggers an immediate run of the given automation type. Requires NOTIFICATION:ADMIN.")
    @PostMapping("/{type}/run")
    @PreAuthorize("hasAuthority('NOTIFICATION:ADMIN')")
    public ApiResponse<AutomationRunResponse> runNow(@PathVariable AutomationType type, HttpServletRequest http) {
        return ApiResponse.success(automationService.runNow(type), "Automation executed", http.getRequestURI());
    }
}
