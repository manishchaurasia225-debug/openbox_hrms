package com.ogm.hrms.automation.evaluator;

import com.ogm.hrms.automation.AutomationEvaluator;
import com.ogm.hrms.automation.AutomationTarget;
import com.ogm.hrms.automation.AutomationVariables;
import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.enums.LeaveStatus;
import com.ogm.hrms.repository.LeaveRequestRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nudges employees who still have a leave request awaiting a decision (status {@code PENDING}), so a
 * stuck request is not forgotten. Approver-directed reminders require the approval hierarchy and are
 * deferred (see decisions.md D-008); reminding the requester is the safe, model-backed behaviour.
 */
@Component
public class LeaveReminderEvaluator implements AutomationEvaluator {

    private final LeaveRequestRepository leaveRequests;

    public LeaveReminderEvaluator(LeaveRequestRepository leaveRequests) {
        this.leaveRequests = leaveRequests;
    }

    @Override
    public AutomationType type() {
        return AutomationType.LEAVE_REMINDER;
    }

    @Override
    public List<AutomationTarget> resolve(AutomationRule rule, LocalDate today) {
        return leaveRequests.findDistinctEmployeesByStatus(LeaveStatus.PENDING).stream()
                .filter(e -> e.getUser() != null)
                .map(this::toTarget)
                .collect(Collectors.toList());
    }

    private AutomationTarget toTarget(Employee employee) {
        return new AutomationTarget(employee.getUser(),
                AutomationVariables.of("name", employee.getFullName(), "code", employee.getEmployeeCode()));
    }
}
