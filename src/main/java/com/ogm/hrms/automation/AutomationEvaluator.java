package com.ogm.hrms.automation;

import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.enums.AutomationType;

import java.time.LocalDate;
import java.util.List;

/**
 * Strategy that resolves the recipients (and their template variables) for a single
 * {@link AutomationType} on a given day. One implementation per occasion keeps each rule's matching
 * logic isolated and independently testable; the engine discovers them via Spring and routes by
 * {@link #type()}. Adding a new occasion means adding an enum value and one evaluator — no engine
 * change.
 */
public interface AutomationEvaluator {

    /** The occasion this evaluator handles. */
    AutomationType type();

    /**
     * Resolve the recipients for {@code today}. Implementations must return only recipients with a
     * usable {@link com.ogm.hrms.entity.User} account and must not dispatch anything themselves.
     */
    List<AutomationTarget> resolve(AutomationRule rule, LocalDate today);
}
