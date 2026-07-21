package com.ogm.hrms.ai;

import java.util.List;

/**
 * Plans how to answer a natural-language query by selecting a tool and extracting its parameters.
 * This is the swappable "intelligence" behind the assistant: the default is a deterministic
 * rule-based provider (works offline, no credentials), and a real LLM adapter (e.g. Anthropic Claude)
 * can be supplied later as a {@code @Primary} bean without changing the tool layer or enforcement.
 */
public interface AiAssistantProvider {

    /** Chooses a tool + parameters for the query, given the tools the caller is allowed to see. */
    AiPlan plan(String query, List<AiTool> availableTools);

    /** Whether this provider uses a real language model (true) or deterministic rules (false). */
    boolean isLlmBacked();
}
