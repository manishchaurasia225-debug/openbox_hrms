package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.automation.AutomationRuleResponse;
import com.ogm.hrms.dto.automation.AutomationRunResponse;
import com.ogm.hrms.dto.automation.UpdateAutomationRuleRequest;
import com.ogm.hrms.enums.AutomationType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Automation Engine (Module 18): manages the configurable automation rules and dispatches engagement
 * notifications (wishes, reminders) through the channel-aware {@link NotificationService}.
 */
public interface AutomationService {

    List<AutomationRuleResponse> listRules();

    AutomationRuleResponse getRule(AutomationType type);

    AutomationRuleResponse updateRule(AutomationType type, UpdateAutomationRuleRequest request);

    /** Manually triggers a rule now, regardless of its enabled flag or a prior run today. */
    AutomationRunResponse runNow(AutomationType type);

    PageResponse<AutomationRunResponse> listRuns(Pageable pageable);

    /**
     * Runs every enabled rule that has not already succeeded for {@code today} (the scheduler entry
     * point). Idempotent across restarts via the run ledger.
     *
     * @return the number of rules executed in this invocation
     */
    int runDueAutomations(LocalDate today);
}
