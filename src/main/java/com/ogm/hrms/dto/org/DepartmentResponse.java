package com.ogm.hrms.dto.org;

import java.time.OffsetDateTime;

/** Department view returned by the API. */
public record DepartmentResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
