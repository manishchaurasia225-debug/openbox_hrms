package com.ogm.hrms.dto.leave;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create/update payload for a leave type. */
public record LeaveTypeRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String description,
        @Min(0) int defaultAnnualQuota,
        Boolean paid,
        Boolean active
) {
}
