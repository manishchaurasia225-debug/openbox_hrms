package com.ogm.hrms.enums;

/**
 * WhatsApp message-template category, mirroring Meta's WhatsApp Business API template taxonomy. The
 * category governs how the provider treats the template (e.g. utility vs marketing messaging rules).
 */
public enum WhatsAppTemplateCategory {

    /** Transactional/operational messages (reminders, status updates, confirmations). */
    UTILITY,

    /** Promotional/engagement messages (wishes, announcements). */
    MARKETING,

    /** One-time passcodes and verification messages. */
    AUTHENTICATION
}
