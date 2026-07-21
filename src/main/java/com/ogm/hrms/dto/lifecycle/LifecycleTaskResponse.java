package com.ogm.hrms.dto.lifecycle;

import java.time.OffsetDateTime;

/** Lifecycle checklist task view. */
public record LifecycleTaskResponse(
        Long id,
        String title,
        int sequence,
        boolean completed,
        OffsetDateTime completedAt,
        String notes
) {
}
