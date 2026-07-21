package com.ogm.hrms.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A natural-language request to the AI assistant. */
public record AiQueryRequest(
        @NotBlank @Size(max = 500) String query
) {
}
