package com.ogm.hrms.dto.lifecycle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Add a custom checklist task to a lifecycle case. */
public record AddLifecycleTaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 300) String notes
) {
}
