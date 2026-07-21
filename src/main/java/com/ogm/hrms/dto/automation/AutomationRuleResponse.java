package com.ogm.hrms.dto.automation;

import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.enums.NotificationChannel;

import java.util.Set;

/** API view of a configured automation rule. */
public record AutomationRuleResponse(
        AutomationType type,
        boolean enabled,
        Set<NotificationChannel> channels,
        String titleTemplate,
        String messageTemplate,
        Integer leadDays
) {
}
