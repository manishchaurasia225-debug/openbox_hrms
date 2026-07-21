package com.ogm.hrms.service.impl;

import com.ogm.hrms.ai.AiAssistantProvider;
import com.ogm.hrms.ai.AiPlan;
import com.ogm.hrms.ai.AiTool;
import com.ogm.hrms.ai.AiToolRequest;
import com.ogm.hrms.ai.AiToolResult;
import com.ogm.hrms.dto.ai.AiQueryRequest;
import com.ogm.hrms.dto.ai.AiResponse;
import com.ogm.hrms.dto.ai.AiToolDescriptor;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.service.AiAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default AI assistant. Enforces the CLAUDE.md AI Rules: it exposes and plans over only the tools the
 * caller is permitted to use (each tool wraps a service, never a repository), so the assistant can
 * neither reach data the user cannot access nor escalate privileges. Tool selection is delegated to a
 * swappable {@link AiAssistantProvider} (rule-based by default; an LLM adapter can replace it).
 */
@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantServiceImpl.class);

    private final List<AiTool> tools;
    private final AiAssistantProvider provider;

    public AiAssistantServiceImpl(List<AiTool> tools, AiAssistantProvider provider) {
        this.tools = tools;
        this.provider = provider;
    }

    @Override
    public AiResponse assist(AiQueryRequest request) {
        Set<String> authorities = currentAuthorities();
        List<AiTool> permitted = tools.stream()
                .filter(tool -> authorities.contains(tool.requiredAuthority()))
                .toList();

        AiPlan plan = provider.plan(request.query(), permitted);
        if (!plan.hasTool()) {
            return new AiResponse(capabilities(permitted), null, null, plan.rationale(), provider.isLlmBacked());
        }

        AiTool tool = permitted.stream()
                .filter(t -> t.name().equals(plan.toolName()))
                .findFirst()
                // Defense in depth: the provider must only pick from permitted tools.
                .orElseThrow(() -> ApiException.forbidden("Not permitted to use the requested capability"));

        log.info("AI assistant invoking tool '{}' (llmBacked={})", tool.name(), provider.isLlmBacked());
        AiToolResult result = tool.execute(new AiToolRequest(request.query(), plan.parameters()));
        return new AiResponse(result.summary(), tool.name(), result.data(), plan.rationale(),
                provider.isLlmBacked());
    }

    @Override
    public List<AiToolDescriptor> availableTools() {
        Set<String> authorities = currentAuthorities();
        return tools.stream()
                .filter(tool -> authorities.contains(tool.requiredAuthority()))
                .map(tool -> new AiToolDescriptor(tool.name(), tool.description(), tool.requiredAuthority()))
                .toList();
    }

    private String capabilities(List<AiTool> permitted) {
        if (permitted.isEmpty()) {
            return "You don't currently have access to any AI capabilities.";
        }
        String names = permitted.stream().map(AiTool::name).collect(Collectors.joining(", "));
        return "I couldn't match that to an action. I can help with: " + names
                + ". For example: 'search employees named John', 'attendance for 2026-07-01', "
                + "or 'generate a leave report as PDF'.";
    }

    private Set<String> currentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
