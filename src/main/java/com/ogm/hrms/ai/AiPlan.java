package com.ogm.hrms.ai;

import java.util.Map;

/**
 * The assistant provider's decision for a query: which tool to invoke, with what parameters, and why.
 * A null {@code toolName} means no tool matched (the assistant answers with its capabilities).
 *
 * @param toolName   the {@link AiTool#name()} to invoke, or null
 * @param parameters extracted tool parameters
 * @param rationale  short explanation of the routing decision (for transparency/audit)
 */
public record AiPlan(String toolName, Map<String, String> parameters, String rationale) {

    public static AiPlan none(String rationale) {
        return new AiPlan(null, Map.of(), rationale);
    }

    public boolean hasTool() {
        return toolName != null;
    }
}
