package com.ogm.hrms.dto.reimbursement;

import jakarta.validation.constraints.Size;

/** Optional remarks for an approve/reject/pay decision. */
public record ReimbursementDecisionRequest(
        @Size(max = 300) String remarks
) {
}
