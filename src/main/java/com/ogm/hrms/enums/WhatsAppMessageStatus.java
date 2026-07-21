package com.ogm.hrms.enums;

/**
 * Lifecycle status of an outbound WhatsApp message, following the Meta WhatsApp Business API delivery
 * signals. Status advances QUEUED → SENT → DELIVERED → READ, or terminates at FAILED.
 */
public enum WhatsAppMessageStatus {

    /** Accepted locally, not yet handed to the provider. */
    QUEUED,

    /** Handed to the provider successfully. */
    SENT,

    /** Provider confirmed delivery to the recipient's device. */
    DELIVERED,

    /** Recipient read the message. */
    READ,

    /** Send or delivery failed; see the failure reason. */
    FAILED
}
