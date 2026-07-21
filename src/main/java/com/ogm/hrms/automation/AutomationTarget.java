package com.ogm.hrms.automation;

import com.ogm.hrms.entity.User;

import java.util.Map;

/**
 * One resolved recipient of an automation, together with the variable values used to render the
 * rule's title/message templates for that recipient.
 *
 * @param recipient the user account to notify (never null — evaluators drop employees without one)
 * @param variables template placeholder values (e.g. {@code name}, {@code code}, {@code date})
 */
public record AutomationTarget(User recipient, Map<String, String> variables) {
}
