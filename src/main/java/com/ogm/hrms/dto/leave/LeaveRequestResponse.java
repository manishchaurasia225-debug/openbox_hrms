package com.ogm.hrms.dto.leave;

import com.ogm.hrms.enums.LeaveStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Leave request view. */
public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        Long leaveTypeId,
        String leaveTypeCode,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal days,
        boolean halfDay,
        String reason,
        LeaveStatus status,
        String managerApprovedBy,
        OffsetDateTime managerApprovedAt,
        String hrApprovedBy,
        OffsetDateTime hrApprovedAt,
        String decisionRemarks
) {
}
