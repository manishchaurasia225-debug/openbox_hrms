package com.ogm.hrms.service;

import com.ogm.hrms.dto.ai.AiQueryRequest;
import com.ogm.hrms.dto.ai.AiResponse;
import com.ogm.hrms.dto.ai.AiToolDescriptor;

import java.util.List;

/**
 * Enterprise AI Assistant (Module 21). Routes a natural-language query to a permission-checked tool
 * that wraps a business service, per the CLAUDE.md AI architecture. The AI never accesses repositories
 * and can never exceed the caller's own permissions.
 */
public interface AiAssistantService {

    AiResponse assist(AiQueryRequest request);

    /** The tools the current caller is permitted to use. */
    List<AiToolDescriptor> availableTools();
}
