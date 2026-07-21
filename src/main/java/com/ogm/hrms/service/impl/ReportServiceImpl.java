package com.ogm.hrms.service.impl;

import com.ogm.hrms.entity.AttendanceRecord;
import com.ogm.hrms.entity.Department;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.LeaveRequest;
import com.ogm.hrms.entity.embed.SalaryInfo;
import com.ogm.hrms.enums.ReportFormat;
import com.ogm.hrms.enums.ReportType;
import com.ogm.hrms.report.ReportData;
import com.ogm.hrms.report.ReportFile;
import com.ogm.hrms.report.ReportRenderer;
import com.ogm.hrms.repository.AttendanceRecordRepository;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.LeaveRequestRepository;
import com.ogm.hrms.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Default {@link ReportService}: builds tabular report data per type, then delegates to the renderer. */
@Service
public class ReportServiceImpl implements ReportService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final ReportRenderer renderer;

    public ReportServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                             LeaveRequestRepository leaveRequestRepository, AttendanceRecordRepository attendanceRepository,
                             ReportRenderer renderer) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.attendanceRepository = attendanceRepository;
        this.renderer = renderer;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportFile generate(ReportType type, ReportFormat format, LocalDate from, LocalDate to) {
        ReportData data = switch (type) {
            case EMPLOYEE -> employeeReport();
            case DEPARTMENT -> departmentReport();
            case LEAVE -> leaveReport();
            case ATTENDANCE -> attendanceReport(from, to);
            case SALARY -> salaryReport();
        };
        return renderer.render(data, format);
    }

    private ReportData employeeReport() {
        List<List<String>> rows = employeeRepository.findByDeletedFalseOrderByEmployeeCodeAsc().stream()
                .map(e -> List.of(nz(e.getEmployeeCode()), nz(e.getFullName()), nz(e.getGender()),
                        e.getDepartment() != null ? e.getDepartment().getName() : "",
                        e.getDesignation() != null ? e.getDesignation().getName() : "",
                        nz(e.getEmploymentStatus()), nz(e.getJoiningDate())))
                .toList();
        return new ReportData("Employee Report",
                List.of("Code", "Name", "Gender", "Department", "Designation", "Status", "Joining Date"), rows);
    }

    private ReportData departmentReport() {
        List<List<String>> rows = departmentRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(d -> List.of(nz(d.getCode()), nz(d.getName()), String.valueOf(d.isActive())))
                .toList();
        return new ReportData("Department Report", List.of("Code", "Name", "Active"), rows);
    }

    private ReportData leaveReport() {
        List<List<String>> rows = leaveRequestRepository.findAllForReport().stream()
                .map(r -> List.of(nz(r.getEmployee().getFullName()), nz(r.getLeaveType().getCode()),
                        nz(r.getFromDate()), nz(r.getToDate()), nz(r.getDays()), nz(r.getStatus())))
                .toList();
        return new ReportData("Leave Report",
                List.of("Employee", "Type", "From", "To", "Days", "Status"), rows);
    }

    private ReportData attendanceReport(LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate end = to != null ? to : LocalDate.now();
        List<List<String>> rows = attendanceRepository.findForReport(start, end).stream()
                .map(a -> List.of(nz(a.getEmployee().getFullName()), nz(a.getAttendanceDate()),
                        nz(a.getAttendanceType()), nz(a.getClockIn()), nz(a.getClockOut()), nz(a.getApprovalStatus())))
                .toList();
        return new ReportData("Attendance Report",
                List.of("Employee", "Date", "Type", "Clock In", "Clock Out", "Approval"), rows);
    }

    private ReportData salaryReport() {
        List<List<String>> rows = employeeRepository.findByDeletedFalseOrderByEmployeeCodeAsc().stream()
                .map(e -> {
                    SalaryInfo s = e.getSalary() != null ? e.getSalary() : new SalaryInfo();
                    BigDecimal gross = sum(s.getBasicSalary(), s.getHra(), s.getSpecialAllowance(),
                            s.getBonus(), s.getIncentives(), s.getOtherAllowances());
                    return List.of(nz(e.getEmployeeCode()), nz(e.getFullName()), money(s.getBasicSalary()),
                            money(s.getHra()), money(s.getSpecialAllowance()), money(s.getBonus()),
                            money(s.getIncentives()), money(s.getOtherAllowances()), money(gross));
                })
                .toList();
        return new ReportData("Salary Report",
                List.of("Code", "Name", "Basic", "HRA", "Special", "Bonus", "Incentives", "Other", "Gross"), rows);
    }

    private BigDecimal sum(BigDecimal... values) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            if (v != null) {
                total = total.add(v);
            }
        }
        return total;
    }

    private String money(BigDecimal value) {
        return value != null ? value.toPlainString() : "0.00";
    }

    private String nz(Object value) {
        return value != null ? value.toString() : "";
    }
}
