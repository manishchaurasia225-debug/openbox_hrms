package com.ogm.hrms.dto.org;

import java.time.OffsetDateTime;

/** Office location view returned by the API. */
public record LocationResponse(
        Long id,
        String code,
        String name,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
