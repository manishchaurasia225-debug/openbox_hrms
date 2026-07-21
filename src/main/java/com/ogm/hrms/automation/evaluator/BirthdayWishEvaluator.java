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

/** Wishes each active employee whose date of birth falls on {@code today}. */
@Component
public class BirthdayWishEvaluator implements AutomationEvaluator {

    private final EmployeeRepository employees;

    public BirthdayWishEvaluator(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Override
    public AutomationType type() {
        return AutomationType.BIRTHDAY_WISH;
    }

    @Override
    public List<AutomationTarget> resolve(AutomationRule rule, LocalDate today) {
        List<AutomationTarget> targets = new ArrayList<>();
        for (Employee employee : employees.findActiveWithUserAndKeyDates()) {
            LocalDate dob = employee.getDateOfBirth();
            if (dob == null || employee.getUser() == null) {
                continue;
            }
            if (dob.getMonthValue() == today.getMonthValue() && dob.getDayOfMonth() == today.getDayOfMonth()) {
                targets.add(new AutomationTarget(employee.getUser(),
                        AutomationVariables.of("name", employee.getFullName(), "code", employee.getEmployeeCode())));
            }
        }
        return targets;
    }
}
