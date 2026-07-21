package com.ogm.hrms.ai;

/**
 * Output of an {@link AiTool}: a human-readable summary and the structured data the tool produced
 * (serialized into the assistant response).
 */
public record AiToolResult(String summary, Object data) {
}
