package com.ogm.hrms.automation;

import java.util.LinkedHashMap;
import java.util.Map;

/** Builds ordered, null-safe template-variable maps for {@link AutomationTarget}s. */
public final class AutomationVariables {

    private AutomationVariables() {
    }

    /**
     * Builds a variable map from alternating key/value arguments; null values become empty strings
     * (so {@code java.util.Map.of} restrictions and downstream NPEs are avoided).
     */
    public static Map<String, String> of(String... keyValuePairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            String value = keyValuePairs[i + 1];
            map.put(keyValuePairs[i], value != null ? value : "");
        }
        return map;
    }
}
