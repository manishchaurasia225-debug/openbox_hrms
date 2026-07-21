package com.ogm.hrms.dto.ai;

/**
 * The assistant's answer to a query.
 *
 * @param answer    human-readable summary of what was done/found
 * @param tool      the tool that was invoked, or null if none matched
 * @param data      structured result produced by the tool (null when no tool ran)
 * @param rationale short explanation of the routing decision (transparency)
 * @param llmBacked whether a real language model produced this (vs. the rule-based default)
 */
public record AiResponse(String answer, String tool, Object data, String rationale, boolean llmBacked) {
}
