package com.ogm.hrms.dto.leave;

import java.math.BigDecimal;

/** Leave balance view for an employee/type/year. */
public record LeaveBalanceResponse(
        Long id,
        Long employeeId,
        Long leaveTypeId,
        String leaveTypeCode,
        int year,
        BigDecimal allocated,
        BigDecimal used,
        BigDecimal remaining
) {
}
