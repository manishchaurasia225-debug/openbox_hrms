package com.ogm.hrms.dto.org;

import java.time.OffsetDateTime;

/** Company profile view returned by the API. */
public record CompanyProfileResponse(
        Long id,
        String legalName,
        String displayName,
        String registrationNumber,
        String email,
        String phone,
        String website,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        String timezone,
        String currency,
        String logoUrl,
        OffsetDateTime updatedAt
) {
}
