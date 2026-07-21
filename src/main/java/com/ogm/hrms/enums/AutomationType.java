package com.ogm.hrms.enums;

/**
 * The catalogue of automated engagement events, per {@code requirements.md} Module 18. Each value is
 * a distinct rule the Automation Engine can evaluate and dispatch through one or more
 * {@link NotificationChannel}s. Rules are configuration (enable/disable, channels), not code, so
 * adding a new occasion means seeding a new {@code automation_rules} row plus an evaluator branch.
 */
public enum AutomationType {

    /** Wish each employee on their date of birth. */
    BIRTHDAY_WISH,

    /** Greet all employees on a company/national holiday flagged as a festival. */
    FESTIVAL_WISH,

    /** Welcome an employee on their joining date. */
    WELCOME_MESSAGE,

    /** Remind employees who have not recorded attendance for the working day. */
    ATTENDANCE_REMINDER,

    /** Remind approvers of leave requests awaiting their decision. */
    LEAVE_REMINDER,

    /** Nudge employees whose mandatory documents are missing. */
    MISSING_DOCUMENTS,

    /** Congratulate an employee on a promotion (designation change). */
    PROMOTION_CONGRATULATIONS,

    /** Remind HR of probations reaching their confirmation window. */
    CONFIRMATION_REMINDER,

    /** Alert on employment/contract records approaching expiry. */
    CONTRACT_EXPIRY
}
