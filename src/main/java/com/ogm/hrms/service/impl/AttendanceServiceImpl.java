package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.attendance.AttendanceCorrectionRequest;
import com.ogm.hrms.dto.attendance.AttendanceResponse;
import com.ogm.hrms.dto.attendance.AttendanceSummaryResponse;
import com.ogm.hrms.dto.attendance.CheckInRequest;
import com.ogm.hrms.entity.AttendanceRecord;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.SystemSetting;
import com.ogm.hrms.enums.ApprovalStatus;
import com.ogm.hrms.enums.AttendanceSource;
import com.ogm.hrms.enums.AttendanceType;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.AttendanceRecordRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.SystemSettingRepository;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.AttendanceService;
import com.ogm.hrms.service.OfficeNetworkService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default {@link AttendanceService}. Enforces one record per employee per day, Wi-Fi/IP validation
 * for office check-in, the WFH workflow (reason + policy-driven approval), corrections, approvals,
 * and monthly summaries. Attendance policy values are read from the configurable system settings.
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Set<AttendanceType> PRESENT_TYPES = EnumSet.of(
            AttendanceType.OFFICE, AttendanceType.WORK_FROM_HOME, AttendanceType.CLIENT_VISIT,
            AttendanceType.BUSINESS_TRAVEL, AttendanceType.COMP_OFF);
    private static final Set<AttendanceType> LEAVE_TYPES = EnumSet.of(
            AttendanceType.CASUAL_LEAVE, AttendanceType.SICK_LEAVE, AttendanceType.EARNED_LEAVE);

    private final AttendanceRecordRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final OfficeNetworkService officeNetworkService;
    private final SystemSettingRepository systemSettingRepository;

    public AttendanceServiceImpl(AttendanceRecordRepository attendanceRepository,
                                 EmployeeRepository employeeRepository, OfficeNetworkService officeNetworkService,
                                 SystemSettingRepository systemSettingRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.officeNetworkService = officeNetworkService;
        this.systemSettingRepository = systemSettingRepository;
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(AuthenticatedUser principal, CheckInRequest request, String ipAddress,
                                      String userAgent) {
        Employee employee = currentEmployee(principal);
        LocalDate today = LocalDate.now();
        if (attendanceRepository.existsByEmployee_IdAndAttendanceDate(employee.getId(), today)) {
            throw ApiException.conflict("Attendance has already been marked for today");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setAttendanceDate(today);
        record.setClockIn(OffsetDateTime.now());
        record.setIpAddress(ipAddress);
        record.setBrowserInfo(truncate(userAgent, 255));
        record.setDeviceInfo("web");

        if (request.mode() == CheckInRequest.Mode.OFFICE) {
            if (!officeNetworkService.isOfficeIp(ipAddress)) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
                        "You are not on the office network. Use work-from-home check-in or connect to the office network.");
            }
            record.setAttendanceType(AttendanceType.OFFICE);
            record.setSource(AttendanceSource.WIFI_IP);
            record.setApprovalStatus(ApprovalStatus.NOT_REQUIRED);
        } else {
            if (request.wfhReason() == null || request.wfhReason().isBlank()) {
                throw ApiException.badRequest("A work-from-home reason is required");
            }
            record.setAttendanceType(AttendanceType.WORK_FROM_HOME);
            record.setSource(AttendanceSource.MANUAL);
            record.setWfhReason(request.wfhReason());
            record.setWorkLocation(request.workLocation());
            record.setExpectedHours(request.expectedHours());
            if (getBooleanSetting("attendance.wfh-auto-approve", false)) {
                record.setApprovalStatus(ApprovalStatus.APPROVED);
                record.setApprovedBy("system");
                record.setApprovedAt(OffsetDateTime.now());
            } else {
                record.setApprovalStatus(ApprovalStatus.PENDING);
            }
        }
        return toResponse(attendanceRepository.save(record));
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(AuthenticatedUser principal) {
        Employee employee = currentEmployee(principal);
        AttendanceRecord record = attendanceRepository
                .findByEmployee_IdAndAttendanceDate(employee.getId(), LocalDate.now())
                .orElseThrow(() -> ApiException.badRequest("No check-in was found for today"));
        if (record.getClockOut() != null) {
            throw ApiException.conflict("You have already checked out today");
        }
        OffsetDateTime out = OffsetDateTime.now();
        record.setClockOut(out);
        if (record.getClockIn() != null) {
            long minutes = Duration.between(record.getClockIn(), out).toMinutes();
            record.setWorkingMinutes((int) minutes);
            int fullDayMinutes = getIntSetting("attendance.working-hours-per-day", 8) * 60;
            record.setHalfDay(minutes < (fullDayMinutes / 2L));
        }
        return toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> myHistory(AuthenticatedUser principal, LocalDate from, LocalDate to,
                                                      Pageable pageable) {
        Employee employee = currentEmployee(principal);
        LocalDate start = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate end = to != null ? to : LocalDate.now();
        return PageResponse.of(attendanceRepository
                .findByEmployee_IdAndAttendanceDateBetween(employee.getId(), start, end, pageable), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> list(Long employeeId, LocalDate date, LocalDate from, LocalDate to,
                                                 Pageable pageable) {
        if (date != null) {
            return PageResponse.of(attendanceRepository.findByAttendanceDate(date, pageable), this::toResponse);
        }
        if (employeeId == null) {
            throw ApiException.badRequest("Provide either 'date' or 'employeeId'");
        }
        LocalDate start = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate end = to != null ? to : LocalDate.now();
        return PageResponse.of(attendanceRepository
                .findByEmployee_IdAndAttendanceDateBetween(employeeId, start, end, pageable), this::toResponse);
    }

    @Override
    @Transactional
    public AttendanceResponse correct(AttendanceCorrectionRequest request) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(request.employeeId())
                .orElseThrow(() -> ApiException.badRequest("Unknown employee: " + request.employeeId()));
        AttendanceRecord record = attendanceRepository
                .findByEmployee_IdAndAttendanceDate(employee.getId(), request.date())
                .orElseGet(AttendanceRecord::new);
        record.setEmployee(employee);
        record.setAttendanceDate(request.date());
        record.setAttendanceType(request.attendanceType());
        record.setSource(AttendanceSource.CORRECTION);
        record.setClockIn(request.clockIn());
        record.setClockOut(request.clockOut());
        if (request.clockIn() != null && request.clockOut() != null) {
            record.setWorkingMinutes((int) Duration.between(request.clockIn(), request.clockOut()).toMinutes());
        }
        record.setRemarks(request.remarks());
        record.setApprovalStatus(ApprovalStatus.NOT_REQUIRED);
        return toResponse(attendanceRepository.save(record));
    }

    @Override
    @Transactional
    public AttendanceResponse decideApproval(Long id, boolean approve, String approver) {
        AttendanceRecord record = attendanceRepository.findWithEmployeeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceRecord", "id", id));
        if (record.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw ApiException.badRequest("This record is not pending approval");
        }
        record.setApprovalStatus(approve ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        record.setApprovedBy(approver);
        record.setApprovedAt(OffsetDateTime.now());
        return toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse monthlySummary(Long employeeId, int year, int month) {
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate last = first.withDayOfMonth(first.lengthOfMonth());
        List<AttendanceRecord> records =
                attendanceRepository.findByEmployee_IdAndAttendanceDateBetween(employeeId, first, last);

        Map<AttendanceType, Long> counts = new EnumMap<>(AttendanceType.class);
        long present = 0;
        long leave = 0;
        long absent = 0;
        long totalMinutes = 0;
        for (AttendanceRecord r : records) {
            counts.merge(r.getAttendanceType(), 1L, Long::sum);
            if (PRESENT_TYPES.contains(r.getAttendanceType())) {
                present++;
            } else if (LEAVE_TYPES.contains(r.getAttendanceType())) {
                leave++;
            } else if (r.getAttendanceType() == AttendanceType.ABSENT) {
                absent++;
            }
            if (r.getWorkingMinutes() != null) {
                totalMinutes += r.getWorkingMinutes();
            }
        }
        return new AttendanceSummaryResponse(employeeId, year, month, present, leave, absent, records.size(),
                totalMinutes, counts);
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Employee currentEmployee(AuthenticatedUser principal) {
        return employeeRepository.findByUser_IdAndDeletedFalse(principal.id())
                .orElseThrow(() -> ApiException.badRequest("Your account is not linked to an employee profile"));
    }

    private String getSetting(String key, String defaultValue) {
        return systemSettingRepository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .filter(v -> v != null && !v.isBlank())
                .orElse(defaultValue);
    }

    private boolean getBooleanSetting(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getSetting(key, String.valueOf(defaultValue)));
    }

    private int getIntSetting(String key, int defaultValue) {
        try {
            return Integer.parseInt(getSetting(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private AttendanceResponse toResponse(AttendanceRecord r) {
        Employee e = r.getEmployee();
        return new AttendanceResponse(r.getId(), e != null ? e.getId() : null,
                e != null ? e.getFullName() : null, r.getAttendanceDate(), r.getAttendanceType(), r.getSource(),
                r.getClockIn(), r.getClockOut(), r.getWorkingMinutes(), r.isLate(), r.isHalfDay(),
                r.getIpAddress(), r.getWfhReason(), r.getWorkLocation(), r.getApprovalStatus(), r.getRemarks());
    }
}
