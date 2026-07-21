package com.ogm.hrms.ai.provider;

import com.ogm.hrms.ai.AiAssistantProvider;
import com.ogm.hrms.ai.AiPlan;
import com.ogm.hrms.ai.AiTool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Deterministic, offline intent router (the default {@link AiAssistantProvider}). It maps a query to
 * one of the available tools by keyword and extracts simple parameters — no external model or
 * credentials required, so the assistant is fully testable. A production LLM adapter can replace this
 * as a {@code @Primary} bean (decision aligned with D-002/D-009). It never bypasses permission
 * enforcement: it only proposes a tool; the service still checks the caller's authority.
 */
@Component
public class RuleBasedAiAssistantProvider implements AiAssistantProvider {

    private static final Pattern ISO_DATE = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");
    private static final Pattern QUOTED = Pattern.compile("[\"']([^\"']+)[\"']");
    private static final Set<String> STOP_WORDS = Set.of("search", "find", "lookup", "look", "up", "for",
            "employee", "employees", "staff", "people", "person", "named", "called", "the", "me", "please",
            "who", "is", "are", "show", "list", "with", "a", "an", "of", "in", "directory");

    @Override
    public AiPlan plan(String query, List<AiTool> availableTools) {
        Set<String> available = availableTools.stream().map(AiTool::name).collect(Collectors.toSet());
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);

        if (q.contains("report") && available.contains("report_generation")) {
            return new AiPlan("report_generation", reportParams(q), "Query mentions a report");
        }
        if ((q.contains("attendance") || q.contains("present") || q.contains("check-in")
                || q.contains("checked in")) && available.contains("attendance_search")) {
            return new AiPlan("attendance_search", attendanceParams(query), "Query mentions attendance");
        }
        if (mentionsEmployeeLookup(q) && available.contains("employee_search")) {
            return new AiPlan("employee_search", Map.of("q", extractSearchTerm(query)),
                    "Query looks like an employee lookup");
        }
        return AiPlan.none("No tool matched the query");
    }

    @Override
    public boolean isLlmBacked() {
        return false;
    }

    private boolean mentionsEmployeeLookup(String q) {
        return q.contains("employee") || q.contains("staff") || q.contains("who")
                || q.contains("find") || q.contains("search") || q.contains("people")
                || q.contains("directory");
    }

    private Map<String, String> reportParams(String q) {
        Map<String, String> params = new HashMap<>();
        for (String type : List.of("EMPLOYEE", "DEPARTMENT", "LEAVE", "ATTENDANCE", "SALARY")) {
            if (q.contains(type.toLowerCase(Locale.ROOT))) {
                params.put("type", type);
                break;
            }
        }
        if (q.contains("excel") || q.contains("xlsx")) {
            params.put("format", "EXCEL");
        } else if (q.contains("pdf")) {
            params.put("format", "PDF");
        } else if (q.contains("csv")) {
            params.put("format", "CSV");
        }
        return params;
    }

    private Map<String, String> attendanceParams(String originalQuery) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = ISO_DATE.matcher(originalQuery);
        if (matcher.find()) {
            params.put("date", matcher.group(1));
        }
        return params;
    }

    private String extractSearchTerm(String originalQuery) {
        if (originalQuery == null) {
            return "";
        }
        Matcher quoted = QUOTED.matcher(originalQuery);
        if (quoted.find()) {
            return quoted.group(1).trim();
        }
        // Otherwise drop filler words and keep the meaningful remainder as the search text.
        String remainder = java.util.Arrays.stream(originalQuery.split("\\s+"))
                .filter(word -> !STOP_WORDS.contains(word.toLowerCase(Locale.ROOT)))
                .collect(Collectors.joining(" "))
                .trim();
        return remainder;
    }
}
