package com.ogm.hrms.automation.evaluator;

import com.ogm.hrms.automation.AutomationEvaluator;
import com.ogm.hrms.automation.AutomationTarget;
import com.ogm.hrms.automation.AutomationVariables;
import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.repository.EmployeeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Alerts on employees whose employment/contract end date is approaching, within the rule's
 * look-ahead ({@code leadDays}, default 30).
 */
@Component
public class ContractExpiryEvaluator implements AutomationEvaluator {

    private static final int DEFAULT_LEAD_DAYS = 30;

    private final EmployeeRepository employees;

    public ContractExpiryEvaluator(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Override
    public AutomationType type() {
        return AutomationType.CONTRACT_EXPIRY;
    }

    @Override
    public List<AutomationTarget> resolve(AutomationRule rule, LocalDate today) {
        int leadDays = rule.getLeadDays() != null ? rule.getLeadDays() : DEFAULT_LEAD_DAYS;
        LocalDate windowEnd = today.plusDays(leadDays);

        List<AutomationTarget> targets = new ArrayList<>();
        for (Employee employee : employees.findActiveWithEndDateBetween(today, windowEnd)) {
            if (employee.getUser() == null) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(today, employee.getEndDate());
            targets.add(new AutomationTarget(employee.getUser(),
                    AutomationVariables.of("name", employee.getFullName(), "code", employee.getEmployeeCode(),
                            "date", String.valueOf(employee.getEndDate()), "days", String.valueOf(days))));
        }
        return targets;
    }
}
