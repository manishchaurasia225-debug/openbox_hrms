package com.ogm.hrms.ai;

/**
 * A capability the AI assistant can invoke. Each tool is a thin wrapper over an existing business
 * <em>service</em> — never a repository — and declares the permission a caller must hold, which the
 * assistant enforces before execution (CLAUDE.md AI Rules: AI → tool-calling → services → repos, with
 * permission checks). This keeps the AI unable to escalate beyond the user's own authorities.
 */
public interface AiTool {

    /** Stable machine name the provider selects by (e.g. {@code employee_search}). */
    String name();

    /** Human/LLM-facing description of what the tool does and when to use it. */
    String description();

    /** The {@code MODULE:ACTION} authority a caller must hold to invoke this tool. */
    String requiredAuthority();

    AiToolResult execute(AiToolRequest request);
}
