package com.ogm.hrms.enums;

/** Outcome of a single {@link AutomationType} evaluation, recorded in the automation run ledger. */
public enum AutomationRunStatus {

    /** The rule was evaluated and any matched recipients were dispatched without error. */
    SUCCESS,

    /** The rule was disabled, had no channels, or matched no recipients — nothing was dispatched. */
    SKIPPED,

    /** Evaluation or dispatch raised an error; see the run detail. */
    FAILED
}
