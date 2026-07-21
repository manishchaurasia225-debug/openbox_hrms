package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.email.EmailTemplateRequest;
import com.ogm.hrms.dto.email.EmailTemplateResponse;
import com.ogm.hrms.dto.email.RenderTemplateRequest;
import com.ogm.hrms.dto.email.RenderedEmailResponse;
import com.ogm.hrms.dto.email.SendTemplateEmailRequest;
import com.ogm.hrms.enums.EmailTemplateCategory;
import com.ogm.hrms.service.EmailTemplateService;
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
import java.util.Map;

/** Email Template Engine API, authorized by the {@code EMAIL} RBAC permissions (matrix §12.3). */
@Tag(name = "Email Templates", description = "Manage reusable email templates and render or send them.")
@RestController
@RequestMapping("/api/v1/email-templates")
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    public EmailTemplateController(EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }

    @Operation(summary = "Create email template",
            description = "Creates a new email template. Requires EMAIL:CREATE.")
    @PostMapping
    @PreAuthorize("hasAuthority('EMAIL:CREATE')")
    public ApiResponse<EmailTemplateResponse> create(@Valid @RequestBody EmailTemplateRequest request,
                                                     HttpServletRequest http) {
        return ApiResponse.success(emailTemplateService.create(request), "Email template created",
                http.getRequestURI());
    }

    @Operation(summary = "List email templates",
            description = "Returns all email templates, optionally filtered by category. Requires EMAIL:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('EMAIL:VIEW')")
    public ApiResponse<List<EmailTemplateResponse>> list(
            @RequestParam(required = false) EmailTemplateCategory category, HttpServletRequest http) {
        return ApiResponse.success(emailTemplateService.list(category), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get email template by id",
            description = "Returns a single email template by its identifier. Requires EMAIL:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMAIL:VIEW')")
    public ApiResponse<EmailTemplateResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(emailTemplateService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update email template",
            description = "Updates an existing email template. Requires EMAIL:EDIT.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMAIL:EDIT')")
    public ApiResponse<EmailTemplateResponse> update(@PathVariable Long id,
            @Valid @RequestBody EmailTemplateRequest request, HttpServletRequest http) {
        return ApiResponse.success(emailTemplateService.update(id, request), "Email template updated",
                http.getRequestURI());
    }

    @Operation(summary = "Delete email template",
            description = "Deletes an email template. Requires EMAIL:DELETE.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMAIL:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        emailTemplateService.delete(id);
        return ApiResponse.success(null, "Email template deleted", http.getRequestURI());
    }

    @Operation(summary = "Preview email template",
            description = "Renders the template with the supplied variables without sending. Requires EMAIL:VIEW.")
    @PostMapping("/{id}/preview")
    @PreAuthorize("hasAuthority('EMAIL:VIEW')")
    public ApiResponse<RenderedEmailResponse> preview(@PathVariable Long id,
            @RequestBody(required = false) RenderTemplateRequest request, HttpServletRequest http) {
        Map<String, String> variables = request != null ? request.variables() : null;
        return ApiResponse.success(emailTemplateService.preview(id, variables), "OK", http.getRequestURI());
    }

    @Operation(summary = "Send templated email",
            description = "Renders the template and sends it to the given recipient. Requires EMAIL:CREATE.")
    @PostMapping("/{id}/send")
    @PreAuthorize("hasAuthority('EMAIL:CREATE')")
    public ApiResponse<RenderedEmailResponse> send(@PathVariable Long id,
            @Valid @RequestBody SendTemplateEmailRequest request, HttpServletRequest http) {
        return ApiResponse.success(emailTemplateService.send(id, request.to(), request.variables()),
                "Email sent", http.getRequestURI());
    }
}
