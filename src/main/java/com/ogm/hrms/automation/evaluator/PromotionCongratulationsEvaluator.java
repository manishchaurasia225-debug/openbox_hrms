package com.ogm.hrms.automation.evaluator;

import com.ogm.hrms.automation.AutomationEvaluator;
import com.ogm.hrms.automation.AutomationTarget;
import com.ogm.hrms.automation.AutomationVariables;
import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.EmployeeTimelineEvent;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.enums.TimelineEventType;
import com.ogm.hrms.repository.EmployeeTimelineEventRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Congratulates employees whose timeline records a promotion dated {@code today}. */
@Component
public class PromotionCongratulationsEvaluator implements AutomationEvaluator {

    private final EmployeeTimelineEventRepository timeline;

    public PromotionCongratulationsEvaluator(EmployeeTimelineEventRepository timeline) {
        this.timeline = timeline;
    }

    @Override
    public AutomationType type() {
        return AutomationType.PROMOTION_CONGRATULATIONS;
    }

    @Override
    public List<AutomationTarget> resolve(AutomationRule rule, LocalDate today) {
        List<AutomationTarget> targets = new ArrayList<>();
        for (EmployeeTimelineEvent event :
                timeline.findByEventTypeAndEventDateWithEmployee(TimelineEventType.PROMOTION, today)) {
            Employee employee = event.getEmployee();
            if (employee == null || employee.getUser() == null) {
                continue;
            }
            targets.add(new AutomationTarget(employee.getUser(),
                    AutomationVariables.of("name", employee.getFullName(), "code", employee.getEmployeeCode(),
                            "title", event.getTitle())));
        }
        return targets;
    }
}
