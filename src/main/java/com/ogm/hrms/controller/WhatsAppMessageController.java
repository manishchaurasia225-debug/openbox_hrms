package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.whatsapp.UpdateMessageStatusRequest;
import com.ogm.hrms.dto.whatsapp.WhatsAppMessageResponse;
import com.ogm.hrms.service.WhatsAppService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WhatsApp message ledger + delivery/read tracking, authorized by the {@code WHATSAPP} RBAC
 * permissions (§12.4). Status updates model the provider (Meta) delivery/read callbacks.
 */
@Tag(name = "WhatsApp Messages", description = "Browse the WhatsApp message ledger and update delivery/read status.")
@RestController
@RequestMapping("/api/v1/whatsapp/messages")
public class WhatsAppMessageController {

    private final WhatsAppService whatsAppService;

    public WhatsAppMessageController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @Operation(summary = "List WhatsApp messages",
            description = "Returns a paginated ledger of sent WhatsApp messages. Requires WHATSAPP:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('WHATSAPP:VIEW')")
    public ApiResponse<PageResponse<WhatsAppMessageResponse>> list(
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(whatsAppService.listMessages(pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get WhatsApp message by id",
            description = "Returns a single WhatsApp message by its identifier. Requires WHATSAPP:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WHATSAPP:VIEW')")
    public ApiResponse<WhatsAppMessageResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(whatsAppService.getMessage(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update message status",
            description = "Records a provider delivery/read status callback for a message. Requires WHATSAPP:EDIT.")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('WHATSAPP:EDIT')")
    public ApiResponse<WhatsAppMessageResponse> updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateMessageStatusRequest request, HttpServletRequest http) {
        return ApiResponse.success(whatsAppService.updateStatus(id, request), "Message status updated",
                http.getRequestURI());
    }
}
