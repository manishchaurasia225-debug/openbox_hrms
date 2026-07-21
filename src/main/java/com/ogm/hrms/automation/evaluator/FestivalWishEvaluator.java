package com.ogm.hrms.automation.evaluator;

import com.ogm.hrms.automation.AutomationEvaluator;
import com.ogm.hrms.automation.AutomationTarget;
import com.ogm.hrms.automation.AutomationVariables;
import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Holiday;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.HolidayRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Greets all active employees when {@code today} is a holiday on the company calendar. The holiday
 * name(s) are exposed as the {@code occasion} template variable. When there is no holiday today the
 * rule matches nobody (and the engine records a SKIPPED run).
 */
@Component
public class FestivalWishEvaluator implements AutomationEvaluator {

    private final HolidayRepository holidays;
    private final EmployeeRepository employees;

    public FestivalWishEvaluator(HolidayRepository holidays, EmployeeRepository employees) {
        this.holidays = holidays;
        this.employees = employees;
    }

    @Override
    public AutomationType type() {
        return AutomationType.FESTIVAL_WISH;
    }

    @Override
    public List<AutomationTarget> resolve(AutomationRule rule, LocalDate today) {
        List<Holiday> todaysHolidays =
                holidays.findByDeletedFalseAndHolidayDateBetweenOrderByHolidayDateAsc(today, today);
        if (todaysHolidays.isEmpty()) {
            return List.of();
        }
        String occasion = todaysHolidays.stream().map(Holiday::getName).collect(Collectors.joining(", "));
        return employees.findAllActiveWithUser().stream()
                .filter(e -> e.getUser() != null)
                .map(e -> new AutomationTarget(e.getUser(),
                        AutomationVariables.of("name", e.getFullName(), "code", e.getEmployeeCode(),
                                "occasion", occasion)))
                .collect(Collectors.toList());
    }
}
