package com.ogm.hrms.enums;

/**
 * Broad functional grouping for email templates, used to organise and filter the template library
 * (Module 19). Categories are structural (a fixed classification), not business rules — the templates
 * themselves (subject, body, variables) are the configurable content.
 */
public enum EmailTemplateCategory {

    /** Account, verification, and password-related mails. */
    ACCOUNT,

    /** Onboarding and welcome communications. */
    ONBOARDING,

    /** Attendance-related notices and reminders. */
    ATTENDANCE,

    /** Leave application, approval, and reminder mails. */
    LEAVE,

    /** Payroll, payslip, and reimbursement mails. */
    PAYROLL,

    /** Document requests, expiry, and renewal notices. */
    DOCUMENT,

    /** Company-wide announcements. */
    ANNOUNCEMENT,

    /** Engagement wishes and automated reminders. */
    ENGAGEMENT,

    /** Anything not covered by a more specific category. */
    GENERAL
}
