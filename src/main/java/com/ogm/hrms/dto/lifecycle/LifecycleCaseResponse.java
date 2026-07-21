package com.ogm.hrms.dto.lifecycle;

import com.ogm.hrms.enums.LifecycleStatus;
import com.ogm.hrms.enums.LifecycleType;

import java.time.LocalDate;
import java.util.List;

/** Lifecycle case view with its checklist. */
public record LifecycleCaseResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LifecycleType type,
        LifecycleStatus status,
        LocalDate initiatedDate,
        LocalDate completedDate,
        String remarks,
        List<LifecycleTaskResponse> tasks
) {
}
