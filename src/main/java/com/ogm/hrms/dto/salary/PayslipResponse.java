package com.ogm.hrms.dto.salary;

import com.ogm.hrms.enums.PayslipStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Payslip view. */
public record PayslipResponse(
        Long id,
        Long employeeId,
        String employeeName,
        int periodYear,
        int periodMonth,
        BigDecimal basic,
        BigDecimal hra,
        BigDecimal specialAllowance,
        BigDecimal bonus,
        BigDecimal incentives,
        BigDecimal otherAllowances,
        BigDecimal grossPay,
        BigDecimal netPay,
        PayslipStatus status,
        OffsetDateTime generatedAt
) {
}
