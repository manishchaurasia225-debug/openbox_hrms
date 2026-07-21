package com.ogm.hrms.dto.org;

import java.time.OffsetDateTime;

/** Designation view returned by the API. */
public record DesignationResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
