package com.ogm.hrms.dto.leave;

/** Leave type view. */
public record LeaveTypeResponse(
        Long id,
        String code,
        String name,
        String description,
        int defaultAnnualQuota,
        boolean paid,
        boolean active
) {
}
