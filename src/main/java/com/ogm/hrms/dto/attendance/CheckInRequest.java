package com.ogm.hrms.dto.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Self-service check-in. {@code OFFICE} is only accepted from the office network (Wi-Fi/IP);
 * {@code WORK_FROM_HOME} requires a reason and may need approval per policy.
 */
public record CheckInRequest(
        @NotNull Mode mode,
        @Size(max = 300) String wfhReason,
        @Size(max = 150) String workLocation,
        Integer expectedHours
) {
    public enum Mode {
        OFFICE,
        WORK_FROM_HOME
    }
}
