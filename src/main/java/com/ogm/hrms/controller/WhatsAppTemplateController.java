package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.email.RenderTemplateRequest;
import com.ogm.hrms.dto.whatsapp.RenderedWhatsAppResponse;
import com.ogm.hrms.dto.whatsapp.SendWhatsAppRequest;
import com.ogm.hrms.dto.whatsapp.WhatsAppMessageResponse;
import com.ogm.hrms.dto.whatsapp.WhatsAppTemplateRequest;
import com.ogm.hrms.dto.whatsapp.WhatsAppTemplateResponse;
import com.ogm.hrms.enums.WhatsAppTemplateCategory;
import com.ogm.hrms.service.WhatsAppService;
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

/** WhatsApp template management + send, authorized by the {@code WHATSAPP} RBAC permissions (§12.4). */
@Tag(name = "WhatsApp Templates", description = "Manage WhatsApp message templates and render or send them.")
@RestController
@RequestMapping("/api/v1/whatsapp/templates")
public class WhatsAppTemplateController {

    private final WhatsAppService whatsAppService;

    public WhatsAppTemplateController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @Operation(summary = "Create WhatsApp template",
            description = "Creates a new WhatsApp message template. Requires WHATSAPP:CREATE.")
    @PostMapping
    @PreAuthorize("hasAuthority('WHATSAPP:CREATE')")
    public ApiResponse<WhatsAppTemplateResponse> create(@Valid @RequestBody WhatsAppTemplateRequest request,
                                                        HttpServletRequest http) {
        return ApiResponse.success(whatsAppService.createTemplate(request), "WhatsApp template created",
                http.getRequestURI());
    }

    @Operation(summary = "List WhatsApp templates",
            description = "Returns all WhatsApp templates, optionally filtered by category. Requires WHATSAPP:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('WHATSAPP:VIEW')")
    public ApiResponse<List<WhatsAppTemplateResponse>> list(
            @RequestParam(required = false) WhatsAppTemplateCategory category, HttpServletRequest http) {
        return ApiResponse.success(whatsAppService.listTemplates(category), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get WhatsApp template by id",
            description = "Returns a single WhatsApp template by its identifier. Requires WHATSAPP:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WHATSAPP:VIEW')")
    public ApiResponse<WhatsAppTemplateResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(whatsAppService.getTemplate(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update WhatsApp template",
            description = "Updates an existing WhatsApp template. Requires WHATSAPP:EDIT.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WHATSAPP:EDIT')")
    public ApiResponse<WhatsAppTemplateResponse> update(@PathVariable Long id,
            @Valid @RequestBody WhatsAppTemplateRequest request, HttpServletRequest http) {
        return ApiResponse.success(whatsAppService.updateTemplate(id, request), "WhatsApp template updated",
                http.getRequestURI());
    }

    @Operation(summary = "Delete WhatsApp template",
            description = "Deletes a WhatsApp template. Requires WHATSAPP:DELETE.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WHATSAPP:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        whatsAppService.deleteTemplate(id);
        return ApiResponse.success(null, "WhatsApp template deleted", http.getRequestURI());
    }

    @Operation(summary = "Preview WhatsApp template",
            description = "Renders the template with the supplied variables without sending. Requires WHATSAPP:VIEW.")
    @PostMapping("/{id}/preview")
    @PreAuthorize("hasAuthority('WHATSAPP:VIEW')")
    public ApiResponse<RenderedWhatsAppResponse> preview(@PathVariable Long id,
            @RequestBody(required = false) RenderTemplateRequest request, HttpServletRequest http) {
        Map<String, String> variables = request != null ? request.variables() : null;
        return ApiResponse.success(whatsAppService.previewTemplate(id, variables), "OK", http.getRequestURI());
    }

    @Operation(summary = "Send templated WhatsApp message",
            description = "Renders the template and sends it as a WhatsApp message to the recipient. Requires WHATSAPP:CREATE.")
    @PostMapping("/{id}/send")
    @PreAuthorize("hasAuthority('WHATSAPP:CREATE')")
    public ApiResponse<WhatsAppMessageResponse> send(@PathVariable Long id,
            @Valid @RequestBody SendWhatsAppRequest request, HttpServletRequest http) {
        return ApiResponse.success(whatsAppService.sendByTemplate(id, request), "WhatsApp message sent",
                http.getRequestURI());
    }
}
