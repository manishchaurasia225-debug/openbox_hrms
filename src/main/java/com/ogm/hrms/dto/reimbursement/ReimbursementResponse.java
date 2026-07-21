package com.ogm.hrms.dto.reimbursement;

import com.ogm.hrms.enums.ExpenseCategory;
import com.ogm.hrms.enums.ReimbursementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Reimbursement claim view. */
public record ReimbursementResponse(
        Long id,
        Long employeeId,
        String employeeName,
        ExpenseCategory category,
        BigDecimal amount,
        LocalDate expenseDate,
        String description,
        Long billDocumentId,
        ReimbursementStatus status,
        String managerDecisionBy,
        OffsetDateTime managerDecisionAt,
        String financeDecisionBy,
        OffsetDateTime financeDecisionAt,
        String decisionRemarks,
        OffsetDateTime paidAt
) {
}
