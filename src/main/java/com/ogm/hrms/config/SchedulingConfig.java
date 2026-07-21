package com.ogm.hrms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's annotation-driven scheduling so {@code @Scheduled} beans (e.g. the Automation
 * Engine's daily job) are registered. Isolated in its own configuration to keep scheduling a single,
 * discoverable switch.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
