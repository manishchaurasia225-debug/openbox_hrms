package com.ogm.hrms.dto.lifecycle;

import com.ogm.hrms.enums.LifecycleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Initiate an onboarding/offboarding case (seeds the default checklist for the type). */
public record InitiateLifecycleRequest(
        @NotNull LifecycleType type,
        @NotNull Long employeeId,
        @Size(max = 500) String remarks
) {
}
