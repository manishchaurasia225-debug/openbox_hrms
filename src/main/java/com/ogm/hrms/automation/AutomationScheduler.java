package com.ogm.hrms.automation;

import com.ogm.hrms.service.AutomationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Fires the Automation Engine once per day. The cron expression and time zone are configurable
 * ({@code hrms.automation.cron}, {@code hrms.automation.zone}); the engine itself is idempotent per
 * (rule, date), so a missed or repeated trigger never double-sends.
 */
@Component
public class AutomationScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutomationScheduler.class);

    private final AutomationService automationService;
    private final ZoneId zone;

    public AutomationScheduler(AutomationService automationService,
                               @Value("${hrms.automation.zone:UTC}") String zone) {
        this.automationService = automationService;
        this.zone = ZoneId.of(zone);
    }

    @Scheduled(cron = "${hrms.automation.cron:0 0 7 * * *}", zone = "${hrms.automation.zone:UTC}")
    public void runDailyAutomations() {
        LocalDate today = LocalDate.now(zone);
        log.debug("Automation scheduler tick for {}", today);
        automationService.runDueAutomations(today);
    }
}
