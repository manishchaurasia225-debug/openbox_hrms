package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.ExpenseCategory;
import com.ogm.hrms.enums.ReimbursementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * A reimbursement claim moving through a two-level approval (manager → finance) and payout. An
 * optional {@code billDocument} links an uploaded receipt. Balance/approval rules: an employee cannot
 * approve their own claim (business rule).
 */
@Entity
@Table(name = "reimbursement_claims", indexes = {
        @Index(name = "idx_reimbursements_employee", columnList = "employee_id"),
        @Index(name = "idx_reimbursements_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class ReimbursementClaim extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reimbursements_employee"))
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ExpenseCategory category;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_document_id", foreignKey = @ForeignKey(name = "fk_reimbursements_document"))
    private Document billDocument;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReimbursementStatus status = ReimbursementStatus.SUBMITTED;

    @Column(name = "manager_decision_by", length = 190)
    private String managerDecisionBy;

    @Column(name = "manager_decision_at")
    private OffsetDateTime managerDecisionAt;

    @Column(name = "finance_decision_by", length = 190)
    private String financeDecisionBy;

    @Column(name = "finance_decision_at")
    private OffsetDateTime financeDecisionAt;

    @Column(name = "decision_remarks", length = 300)
    private String decisionRemarks;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;
}
