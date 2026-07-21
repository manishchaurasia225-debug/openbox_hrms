package com.ogm.hrms.dto.employee;

import com.ogm.hrms.enums.TimelineEventType;

import java.time.LocalDate;

/** Timeline event view. */
public record TimelineEventResponse(
        Long id,
        TimelineEventType eventType,
        String title,
        String description,
        LocalDate eventDate
) {
}
