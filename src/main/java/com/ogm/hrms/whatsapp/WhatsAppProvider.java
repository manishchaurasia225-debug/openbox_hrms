package com.ogm.hrms.whatsapp;

/**
 * Abstraction over the WhatsApp delivery backend (e.g. the Meta WhatsApp Business API). Business code
 * depends only on this contract; the concrete provider is a swappable adapter. When no provider is
 * configured, {@link LoggingWhatsAppProvider} is the default so flows work in dev/test without any
 * external calls or credentials (the same approach as the email fallback — decision D-002).
 */
public interface WhatsAppProvider {

    /**
     * Sends a plain-text WhatsApp message to an E.164 phone number.
     *
     * @return the send result (accepted + provider message id, or failed + reason)
     */
    WhatsAppSendResult send(String toPhone, String body);

    /** Whether this provider actually transmits (true) or only simulates/logs (false). */
    boolean isLive();
}
