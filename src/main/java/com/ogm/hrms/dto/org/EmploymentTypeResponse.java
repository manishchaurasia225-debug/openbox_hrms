package com.ogm.hrms.dto.org;

import java.time.OffsetDateTime;

/** Employment type view returned by the API. */
public record EmploymentTypeResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
