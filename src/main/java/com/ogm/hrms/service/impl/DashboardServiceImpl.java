package com.ogm.hrms.service.impl;

import com.ogm.hrms.dto.communication.AnnouncementResponse;
import com.ogm.hrms.dto.dashboard.CountEntry;
import com.ogm.hrms.dto.dashboard.EmployeeDashboardResponse;
import com.ogm.hrms.dto.dashboard.HrDashboardResponse;
import com.ogm.hrms.dto.dashboard.PersonDate;
import com.ogm.hrms.dto.holiday.HolidayResponse;
import com.ogm.hrms.dto.leave.LeaveBalanceResponse;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.enums.ApprovalStatus;
import com.ogm.hrms.enums.AttendanceType;
import com.ogm.hrms.enums.Gender;
import com.ogm.hrms.enums.LeaveStatus;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.repository.AttendanceRecordRepository;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.LeaveBalanceRepository;
import com.ogm.hrms.repository.LeaveRequestRepository;
import com.ogm.hrms.repository.NotificationRepository;
import com.ogm.hrms.repository.PayslipRepository;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.AnnouncementService;
import com.ogm.hrms.service.AttendanceService;
import com.ogm.hrms.service.DashboardService;
import com.ogm.hrms.service.HolidayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Default {@link DashboardService}. Aggregates read-only metrics across modules. */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Set<AttendanceType> PRESENT_TYPES = EnumSet.of(AttendanceType.OFFICE,
            AttendanceType.WORK_FROM_HOME, AttendanceType.CLIENT_VISIT, AttendanceType.BUSINESS_TRAVEL,
            AttendanceType.COMP_OFF);
    private static final int UPCOMING_WINDOW_DAYS = 30;

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final NotificationRepository notificationRepository;
    private final PayslipRepository payslipRepository;
    private final AttendanceService attendanceService;
    private final AnnouncementService announcementService;
    private final HolidayService holidayService;

    public DashboardServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                                LeaveRequestRepository leaveRequestRepository, LeaveBalanceRepository leaveBalanceRepository,
                                AttendanceRecordRepository attendanceRepository, NotificationRepository notificationRepository,
                                PayslipRepository payslipRepository, AttendanceService attendanceService,
                                AnnouncementService announcementService, HolidayService holidayService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.attendanceRepository = attendanceRepository;
        this.notificationRepository = notificationRepository;
        this.payslipRepository = payslipRepository;
        this.attendanceService = attendanceService;
        this.announcementService = announcementService;
        this.holidayService = holidayService;
    }

    @Override
    @Transactional(readOnly = true)
    public HrDashboardResponse hrDashboard() {
        LocalDate today = LocalDate.now();
        long presentToday = attendanceRepository.countByAttendanceDateAndAttendanceTypeIn(today, PRESENT_TYPES);
        long onLeaveToday = leaveRequestRepository
                .countByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(LeaveStatus.APPROVED, today, today);
        long pendingLeave = leaveRequestRepository.countByStatusIn(
                List.of(LeaveStatus.PENDING, LeaveStatus.MANAGER_APPROVED));
        long pendingWfh = attendanceRepository.countByApprovalStatus(ApprovalStatus.PENDING);

        List<PersonDate> birthdays = new ArrayList<>();
        List<PersonDate> anniversaries = new ArrayList<>();
        for (Employee e : employeeRepository.findActiveWithKeyDates()) {
            addIfUpcoming(birthdays, e, e.getDateOfBirth(), today);
            addIfUpcoming(anniversaries, e, e.getJoiningDate(), today);
        }
        birthdays.sort(Comparator.comparing(PersonDate::date));
        anniversaries.sort(Comparator.comparing(PersonDate::date));

        return new HrDashboardResponse(
                employeeRepository.countByDeletedFalse(),
                departmentRepository.countByDeletedFalse(),
                presentToday, onLeaveToday, pendingLeave, pendingWfh,
                employeeRepository.countByDeletedFalseAndJoiningDateGreaterThanEqual(today.minusDays(30)),
                birthdays, anniversaries,
                toDistribution(employeeRepository.departmentDistribution(), false),
                toDistribution(employeeRepository.genderDistribution(), true));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDashboardResponse myDashboard(AuthenticatedUser principal) {
        Employee e = employeeRepository.findByIdAndDeletedFalse(currentEmployeeId(principal))
                .orElseThrow(() -> ApiException.badRequest("Your account is not linked to an employee profile"));
        LocalDate today = LocalDate.now();

        List<LeaveBalanceResponse> balances = leaveBalanceRepository
                .findByEmployee_IdAndYear(e.getId(), today.getYear()).stream()
                .map(b -> new LeaveBalanceResponse(b.getId(), e.getId(), b.getLeaveType().getId(),
                        b.getLeaveType().getCode(), b.getYear(), b.getAllocated(), b.getUsed(), b.remaining()))
                .toList();

        List<HolidayResponse> upcomingHolidays = holidayService.calendar(null, today, today.plusDays(60), null);
        List<AnnouncementResponse> announcements = announcementService.feed().stream().limit(5).toList();

        return new EmployeeDashboardResponse(
                e.getId(), e.getFullName(), e.getEmployeeCode(),
                e.getDepartment() != null ? e.getDepartment().getName() : null,
                e.getDesignation() != null ? e.getDesignation().getName() : null,
                profileCompletion(e),
                attendanceService.monthlySummary(e.getId(), today.getYear(), today.getMonthValue()),
                balances, upcomingHolidays, announcements,
                notificationRepository.countByRecipient_IdAndReadFalseAndDeletedFalse(principal.id()),
                payslipRepository.countByEmployee_IdAndDeletedFalse(e.getId()));
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Long currentEmployeeId(AuthenticatedUser principal) {
        return employeeRepository.findByUser_IdAndDeletedFalse(principal.id())
                .map(Employee::getId)
                .orElseThrow(() -> ApiException.badRequest("Your account is not linked to an employee profile"));
    }

    private void addIfUpcoming(List<PersonDate> target, Employee e, LocalDate date, LocalDate today) {
        if (date == null) {
            return;
        }
        MonthDay monthDay = MonthDay.from(date);
        LocalDate next = monthDay.atYear(today.getYear());
        if (next.isBefore(today)) {
            next = monthDay.atYear(today.getYear() + 1);
        }
        if (!next.isAfter(today.plusDays(UPCOMING_WINDOW_DAYS))) {
            target.add(new PersonDate(e.getId(), e.getFullName(), next));
        }
    }

    private List<CountEntry> toDistribution(List<Object[]> rows, boolean gender) {
        List<CountEntry> result = new ArrayList<>();
        for (Object[] row : rows) {
            String label;
            if (gender) {
                label = row[0] != null ? ((Gender) row[0]).name() : "UNDISCLOSED";
            } else {
                label = row[0] != null ? row[0].toString() : "Unassigned";
            }
            result.add(new CountEntry(label, ((Number) row[1]).longValue()));
        }
        return result;
    }

    private int profileCompletion(Employee e) {
        int total = 9;
        int filled = 0;
        if (isSet(e.getFullName())) filled++;
        if (e.getGender() != null) filled++;
        if (e.getDateOfBirth() != null) filled++;
        if (e.getContact() != null && isSet(e.getContact().getMobile())) filled++;
        if (e.getContact() != null && isSet(e.getContact().getOfficialEmail())) filled++;
        if (e.getDepartment() != null) filled++;
        if (e.getDesignation() != null) filled++;
        if (e.getEmploymentType() != null) filled++;
        if (e.getJoiningDate() != null) filled++;
        return (int) Math.round(100.0 * filled / total);
    }

    private boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
