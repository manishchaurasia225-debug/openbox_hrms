package com.ogm.hrms.dto.email;

import java.util.Map;

/** Variable values used to render a template for preview. */
public record RenderTemplateRequest(
        Map<String, String> variables
) {
}
