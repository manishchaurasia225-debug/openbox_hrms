package com.ogm.hrms.dto.org;

import java.time.OffsetDateTime;

/** System setting view returned by the API. */
public record SystemSettingResponse(
        Long id,
        String key,
        String value,
        String category,
        String description,
        boolean editable,
        OffsetDateTime updatedAt
) {
}
