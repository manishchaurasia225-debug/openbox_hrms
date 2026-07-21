package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.dto.ai.AiQueryRequest;
import com.ogm.hrms.dto.ai.AiResponse;
import com.ogm.hrms.dto.ai.AiToolDescriptor;
import com.ogm.hrms.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Enterprise AI Assistant API. Gated by {@code AI:VIEW}; each underlying tool additionally enforces
 * its own module permission, so the assistant can never exceed the caller's access (§13.1).
 */
@Tag(name = "AI Assistant", description = "Natural-language HR assistant that executes permission-checked tool calls.")
@RestController
@RequestMapping("/api/v1/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @Operation(summary = "Ask the AI assistant",
            description = "Submits a natural-language query; the assistant answers using tools bounded by the caller's permissions. Requires AI:VIEW.")
    @PostMapping("/assistant")
    @PreAuthorize("hasAuthority('AI:VIEW')")
    public ApiResponse<AiResponse> assist(@Valid @RequestBody AiQueryRequest request, HttpServletRequest http) {
        return ApiResponse.success(aiAssistantService.assist(request), "OK", http.getRequestURI());
    }

    @Operation(summary = "List available AI tools",
            description = "Returns the descriptors of tools the assistant can invoke. Requires AI:VIEW.")
    @GetMapping("/tools")
    @PreAuthorize("hasAuthority('AI:VIEW')")
    public ApiResponse<List<AiToolDescriptor>> tools(HttpServletRequest http) {
        return ApiResponse.success(aiAssistantService.availableTools(), "OK", http.getRequestURI());
    }
}
