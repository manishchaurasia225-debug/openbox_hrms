package com.ogm.hrms.dto.org;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Editable company profile fields. */
public record CompanyProfileRequest(
        @NotBlank @Size(max = 200) String legalName,
        @NotBlank @Size(max = 120) String displayName,
        @Size(max = 60) String registrationNumber,
        @Email @Size(max = 190) String email,
        @Size(max = 30) String phone,
        @Size(max = 200) String website,
        @Size(max = 200) String addressLine1,
        @Size(max = 200) String addressLine2,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 100) String country,
        @Size(max = 20) String postalCode,
        @Size(max = 60) String timezone,
        @Size(max = 10) String currency,
        @Size(max = 500) String logoUrl
) {
}
