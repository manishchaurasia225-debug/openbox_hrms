package com.ogm.hrms.dto.leave;

import jakarta.validation.constraints.Size;

/** Optional remarks accompanying an approve/reject decision. */
public record LeaveDecisionRequest(
        @Size(max = 300) String remarks
) {
}
