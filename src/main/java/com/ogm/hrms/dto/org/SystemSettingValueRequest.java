package com.ogm.hrms.dto.org;

import jakarta.validation.constraints.Size;

/** Update payload carrying the new value for an existing system setting. */
public record SystemSettingValueRequest(
        @Size(max = 2000) String value
) {
}
