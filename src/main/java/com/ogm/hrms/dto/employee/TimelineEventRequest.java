package com.ogm.hrms.dto.employee;

import com.ogm.hrms.enums.TimelineEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Create payload for a manual employee timeline event. */
public record TimelineEventRequest(
        @NotNull TimelineEventType eventType,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 500) String description,
        LocalDate eventDate
) {
}
