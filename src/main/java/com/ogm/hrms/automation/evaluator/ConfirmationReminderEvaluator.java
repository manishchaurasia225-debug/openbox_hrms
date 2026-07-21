package com.ogm.hrms.automation.evaluator;

import com.ogm.hrms.automation.AutomationEvaluator;
import com.ogm.hrms.automation.AutomationTarget;
import com.ogm.hrms.automation.AutomationVariables;
import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.ProbationRecord;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.enums.ProbationStatus;
import com.ogm.hrms.repository.ProbationRecordRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Reminds employees whose probation reaches its confirmation window within the rule's look-ahead
 * ({@code leadDays}, default 7). Only active probations ({@code IN_PROBATION}/{@code EXTENDED}) are
 * considered.
 */
@Component
public class ConfirmationReminderEvaluator implements AutomationEvaluator {

    private static final int DEFAULT_LEAD_DAYS = 7;

    private final ProbationRecordRepository probations;

    public ConfirmationReminderEvaluator(ProbationRecordRepository probations) {
        this.probations = probations;
    }

    @Override
    public AutomationType type() {
        return AutomationType.CONFIRMATION_REMINDER;
    }

    @Override
    public List<AutomationTarget> resolve(AutomationRule rule, LocalDate today) {
        int leadDays = rule.getLeadDays() != null ? rule.getLeadDays() : DEFAULT_LEAD_DAYS;
        LocalDate windowEnd = today.plusDays(leadDays);

        List<AutomationTarget> targets = new ArrayList<>();
        List<ProbationRecord> due = probations.findByStatusInAndEndDateBetweenOrderByEndDateAsc(
                EnumSet.of(ProbationStatus.IN_PROBATION, ProbationStatus.EXTENDED), today, windowEnd);
        for (ProbationRecord record : due) {
            Employee employee = record.getEmployee();
            if (employee == null || employee.getUser() == null) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(today, record.getEndDate());
            targets.add(new AutomationTarget(employee.getUser(),
                    AutomationVariables.of("name", employee.getFullName(), "code", employee.getEmployeeCode(),
                            "date", String.valueOf(record.getEndDate()), "days", String.valueOf(days))));
        }
        return targets;
    }
}
