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
import java.util.ArrayList;
import java.util.List;

/** Welcomes each active employee whose joining date is {@code today}. */
@Component
public class WelcomeMessageEvaluator implements AutomationEvaluator {

    private final EmployeeRepository employees;

    public WelcomeMessageEvaluator(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Override
    public AutomationType type() {
        return AutomationType.WELCOME_MESSAGE;
    }

    @Override
    public List<AutomationTarget> resolve(AutomationRule rule, LocalDate today) {
        List<AutomationTarget> targets = new ArrayList<>();
        for (Employee employee : employees.findActiveWithUserAndKeyDates()) {
            if (employee.getUser() == null || !today.equals(employee.getJoiningDate())) {
                continue;
            }
            targets.add(new AutomationTarget(employee.getUser(),
                    AutomationVariables.of("name", employee.getFullName(), "code", employee.getEmployeeCode())));
        }
        return targets;
    }
}
