package com.ogm.hrms.ai;

import java.util.Map;

/**
 * Input to an {@link AiTool}: the user's raw natural-language query plus structured parameters the
 * assistant provider extracted from it.
 */
public record AiToolRequest(String query, Map<String, String> parameters) {

    public String param(String key, String defaultValue) {
        String value = parameters != null ? parameters.get(key) : null;
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
