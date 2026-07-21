package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.LeaveStatus;
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
 * A leave application moving through a two-level approval workflow (manager → HR). Balance is
 * deducted only on final HR approval and restored if a previously-approved leave is cancelled.
 */
@Entity
@Table(name = "leave_requests", indexes = {
        @Index(name = "idx_leave_requests_employee", columnList = "employee_id"),
        @Index(name = "idx_leave_requests_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class LeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_leave_requests_employee"))
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_leave_requests_type"))
    private LeaveType leaveType;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "days", nullable = false, precision = 6, scale = 1)
    private BigDecimal days;

    @Column(name = "half_day", nullable = false)
    private boolean halfDay = false;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "manager_approved_by", length = 190)
    private String managerApprovedBy;

    @Column(name = "manager_approved_at")
    private OffsetDateTime managerApprovedAt;

    @Column(name = "hr_approved_by", length = 190)
    private String hrApprovedBy;

    @Column(name = "hr_approved_at")
    private OffsetDateTime hrApprovedAt;

    @Column(name = "decision_remarks", length = 300)
    private String decisionRemarks;
}
