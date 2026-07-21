package com.ogm.hrms.dto.org;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create payload for a new system setting. */
public record SystemSettingRequest(
        @NotBlank @Size(max = 120) String key,
        @Size(max = 2000) String value,
        @Size(max = 80) String category,
        @Size(max = 255) String description
) {
}
