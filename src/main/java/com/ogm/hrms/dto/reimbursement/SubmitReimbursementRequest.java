package com.ogm.hrms.dto.reimbursement;

import com.ogm.hrms.enums.ExpenseCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** An employee's reimbursement claim submission. */
public record SubmitReimbursementRequest(
        @NotNull ExpenseCategory category,
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate expenseDate,
        @Size(max = 500) String description,
        Long billDocumentId
) {
}
