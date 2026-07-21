package com.ogm.hrms.dto.automation;

import com.ogm.hrms.enums.AutomationRunStatus;
import com.ogm.hrms.enums.AutomationType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** API view of a single automation execution from the run ledger. */
public record AutomationRunResponse(
        Long id,
        AutomationType type,
        LocalDate runDate,
        AutomationRunStatus status,
        boolean manual,
        int matched,
        int dispatched,
        String detail,
        OffsetDateTime executedAt
) {
}
