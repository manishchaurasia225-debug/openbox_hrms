package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.ApprovalStatus;
import com.ogm.hrms.enums.AttendanceSource;
import com.ogm.hrms.enums.AttendanceType;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * A single day's attendance for an employee (at most one per day — {@code uk_attendance_emp_date}).
 * Location capture is Wi-Fi/IP based (no GPS): the public IP, device, and browser are recorded, and
 * the source indicates how it was captured. WFH entries carry reason/location and an approval state.
 */
@Entity
@Table(name = "attendance_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_attendance_emp_date", columnNames = {"employee_id", "attendance_date"}),
        indexes = {
                @Index(name = "idx_attendance_employee", columnList = "employee_id"),
                @Index(name = "idx_attendance_date", columnList = "attendance_date")
        })
@Getter
@Setter
@NoArgsConstructor
public class AttendanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attendance_employee"))
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_type", nullable = false, length = 30)
    private AttendanceType attendanceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private AttendanceSource source;

    @Column(name = "clock_in")
    private OffsetDateTime clockIn;

    @Column(name = "clock_out")
    private OffsetDateTime clockOut;

    @Column(name = "working_minutes")
    private Integer workingMinutes;

    @Column(name = "late", nullable = false)
    private boolean late = false;

    @Column(name = "half_day", nullable = false)
    private boolean halfDay = false;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "browser_info", length = 255)
    private String browserInfo;

    @Column(name = "wfh_reason", length = 300)
    private String wfhReason;

    @Column(name = "work_location", length = 150)
    private String workLocation;

    @Column(name = "expected_hours")
    private Integer expectedHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.NOT_REQUIRED;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "remarks", length = 300)
    private String remarks;
}
