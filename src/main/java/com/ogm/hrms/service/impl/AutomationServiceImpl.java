package com.ogm.hrms.service.impl;

import com.ogm.hrms.automation.AutomationEvaluator;
import com.ogm.hrms.automation.AutomationTarget;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.automation.AutomationRuleResponse;
import com.ogm.hrms.dto.automation.AutomationRunResponse;
import com.ogm.hrms.dto.automation.UpdateAutomationRuleRequest;
import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.entity.AutomationRun;
import com.ogm.hrms.enums.AutomationRunStatus;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.enums.NotificationChannel;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.repository.AutomationRuleRepository;
import com.ogm.hrms.repository.AutomationRunRepository;
import com.ogm.hrms.service.AutomationService;
import com.ogm.hrms.service.NotificationService;
import com.ogm.hrms.util.PlaceholderRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Default Automation Engine. Rules are configuration; matching logic lives in per-type
 * {@link AutomationEvaluator}s (discovered via Spring and routed by type). Dispatch always goes
 * through the {@link NotificationService} producer API, so every generated message is persisted and
 * visible in the in-app notification centre regardless of channel.
 */
@Service
public class AutomationServiceImpl implements AutomationService {

    private static final Logger log = LoggerFactory.getLogger(AutomationServiceImpl.class);
    private static final int MAX_DETAIL = 500;

    private final AutomationRuleRepository ruleRepository;
    private final AutomationRunRepository runRepository;
    private final NotificationService notificationService;
    private final Map<AutomationType, AutomationEvaluator> evaluators = new EnumMap<>(AutomationType.class);

    public AutomationServiceImpl(AutomationRuleRepository ruleRepository, AutomationRunRepository runRepository,
                                 NotificationService notificationService, List<AutomationEvaluator> evaluators) {
        this.ruleRepository = ruleRepository;
        this.runRepository = runRepository;
        this.notificationService = notificationService;
        for (AutomationEvaluator evaluator : evaluators) {
            this.evaluators.put(evaluator.type(), evaluator);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutomationRuleResponse> listRules() {
        return ruleRepository.findByDeletedFalseOrderByTypeAsc().stream().map(this::toRuleResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AutomationRuleResponse getRule(AutomationType type) {
        return toRuleResponse(requireRule(type));
    }

    @Override
    @Transactional
    public AutomationRuleResponse updateRule(AutomationType type, UpdateAutomationRuleRequest request) {
        AutomationRule rule = requireRule(type);
        if (request.enabled() != null) {
            rule.setEnabled(request.enabled());
        }
        if (request.channels() != null) {
            rule.setChannels(new java.util.LinkedHashSet<>(request.channels()));
        }
        if (request.titleTemplate() != null) {
            rule.setTitleTemplate(request.titleTemplate());
        }
        if (request.messageTemplate() != null) {
            rule.setMessageTemplate(request.messageTemplate());
        }
        if (request.leadDays() != null) {
            rule.setLeadDays(request.leadDays());
        }
        log.info("Automation rule {} updated (enabled={}, channels={})", type, rule.isEnabled(), rule.getChannels());
        return toRuleResponse(ruleRepository.save(rule));
    }

    @Override
    @Transactional
    public AutomationRunResponse runNow(AutomationType type) {
        AutomationRule rule = requireRule(type);
        return toRunResponse(execute(rule, LocalDate.now(), true));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AutomationRunResponse> listRuns(Pageable pageable) {
        return PageResponse.of(runRepository.findByOrderByCreatedAtDesc(pageable), this::toRunResponse);
    }

    @Override
    @Transactional
    public int runDueAutomations(LocalDate today) {
        int executed = 0;
        for (AutomationRule rule : ruleRepository.findByEnabledTrueAndDeletedFalse()) {
            if (runRepository.existsByRule_IdAndRunDateAndStatus(rule.getId(), today, AutomationRunStatus.SUCCESS)) {
                continue;
            }
            execute(rule, today, false);
            executed++;
        }
        log.info("Automation scheduler executed {} rule(s) for {}", executed, today);
        return executed;
    }

    /** Evaluates and dispatches a single rule, recording the outcome in the run ledger. */
    private AutomationRun execute(AutomationRule rule, LocalDate today, boolean manual) {
        AutomationRun run = new AutomationRun();
        run.setRule(rule);
        run.setRunDate(today);
        run.setManual(manual);
        try {
            AutomationEvaluator evaluator = evaluators.get(rule.getType());
            if (evaluator == null) {
                return persist(run, AutomationRunStatus.SKIPPED, 0, 0, "No evaluator registered for type");
            }
            if (rule.getChannels().isEmpty()) {
                return persist(run, AutomationRunStatus.SKIPPED, 0, 0, "No channels configured");
            }
            List<AutomationTarget> targets = evaluator.resolve(rule, today);
            if (targets.isEmpty()) {
                return persist(run, AutomationRunStatus.SKIPPED, 0, 0, "No matching recipients");
            }
            int dispatched = dispatch(rule, targets);
            return persist(run, AutomationRunStatus.SUCCESS, targets.size(), dispatched,
                    "Dispatched " + dispatched + " notification(s) to " + targets.size() + " recipient(s)");
        } catch (RuntimeException ex) {
            log.error("Automation rule {} failed: {}", rule.getType(), ex.getMessage(), ex);
            return persist(run, AutomationRunStatus.FAILED, run.getMatched(), run.getDispatched(),
                    "Error: " + ex.getMessage());
        }
    }

    private int dispatch(AutomationRule rule, List<AutomationTarget> targets) {
        String referenceType = "AUTOMATION:" + rule.getType().name();
        int dispatched = 0;
        for (AutomationTarget target : targets) {
            String title = PlaceholderRenderer.render(rule.getTitleTemplate(), target.variables());
            String message = PlaceholderRenderer.render(rule.getMessageTemplate(), target.variables());
            for (NotificationChannel channel : rule.getChannels()) {
                notificationService.notify(target.recipient(), channel, title, message, referenceType, rule.getId());
                dispatched++;
            }
        }
        return dispatched;
    }

    private AutomationRun persist(AutomationRun run, AutomationRunStatus status, int matched, int dispatched,
                                  String detail) {
        run.setStatus(status);
        run.setMatched(matched);
        run.setDispatched(dispatched);
        run.setDetail(detail != null && detail.length() > MAX_DETAIL ? detail.substring(0, MAX_DETAIL) : detail);
        return runRepository.save(run);
    }

    private AutomationRule requireRule(AutomationType type) {
        return ruleRepository.findByType(type)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Automation rule not found: " + type));
    }

    private AutomationRuleResponse toRuleResponse(AutomationRule rule) {
        return new AutomationRuleResponse(rule.getType(), rule.isEnabled(), rule.getChannels(),
                rule.getTitleTemplate(), rule.getMessageTemplate(), rule.getLeadDays());
    }

    private AutomationRunResponse toRunResponse(AutomationRun run) {
        return new AutomationRunResponse(run.getId(), run.getRule().getType(), run.getRunDate(), run.getStatus(),
                run.isManual(), run.getMatched(), run.getDispatched(), run.getDetail(), run.getCreatedAt());
    }
}
