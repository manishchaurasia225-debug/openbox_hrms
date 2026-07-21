package com.ogm.hrms.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal, dependency-free template rendering: substitutes {@code {token}} placeholders with values
 * from a variable map. Unknown or null-valued tokens render as empty strings. This is deliberately
 * not a general templating engine (that is Module 19's Email Template Engine); it exists so short
 * automation/notification strings can carry a few named values without string concatenation.
 */
public final class PlaceholderRenderer {

    private static final Pattern TOKEN = Pattern.compile("\\{(\\w+)}");

    private PlaceholderRenderer() {
    }

    public static String render(String template, Map<String, String> variables) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        Map<String, String> vars = variables != null ? variables : Map.of();
        Matcher matcher = TOKEN.matcher(template);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String value = vars.get(matcher.group(1));
            matcher.appendReplacement(out, Matcher.quoteReplacement(value != null ? value : ""));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
