package com.ogm.hrms.dto.automation;

import com.ogm.hrms.enums.NotificationChannel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Partial update of an automation rule. All fields are optional (PATCH semantics): a null field is
 * left unchanged. Channels, when provided, replace the existing set.
 */
public record UpdateAutomationRuleRequest(
        Boolean enabled,

        Set<NotificationChannel> channels,

        @Size(max = 200, message = "Title template must be at most 200 characters")
        String titleTemplate,

        @Size(max = 1000, message = "Message template must be at most 1000 characters")
        String messageTemplate,

        @Min(value = 0, message = "Lead days cannot be negative")
        @Max(value = 365, message = "Lead days cannot exceed 365")
        Integer leadDays
) {
}
