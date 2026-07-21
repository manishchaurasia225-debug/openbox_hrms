package com.ogm.hrms.automation.evaluator;

import com.ogm.hrms.automation.AutomationEvaluator;
import com.ogm.hrms.automation.AutomationTarget;
import com.ogm.hrms.automation.AutomationVariables;
import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.repository.AttendanceRecordRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reminds active employees who have no attendance record for {@code today} to mark it. */
@Component
public class AttendanceReminderEvaluator implements AutomationEvaluator {

    private final EmployeeRepository employees;
    private final AttendanceRecordRepository attendance;

    public AttendanceReminderEvaluator(EmployeeRepository employees, AttendanceRecordRepository attendance) {
        this.employees = employees;
        this.attendance = attendance;
    }

    @Override
    public AutomationType type() {
        return AutomationType.ATTENDANCE_REMINDER;
    }

    @Override
    public List<AutomationTarget> resolve(AutomationRule rule, LocalDate today) {
        List<AutomationTarget> targets = new ArrayList<>();
        for (Employee employee : employees.findAllActiveWithUser()) {
            if (employee.getUser() == null
                    || attendance.existsByEmployee_IdAndAttendanceDate(employee.getId(), today)) {
                continue;
            }
            targets.add(new AutomationTarget(employee.getUser(),
                    AutomationVariables.of("name", employee.getFullName(), "code", employee.getEmployeeCode(),
                            "date", today.toString())));
        }
        return targets;
    }
}
