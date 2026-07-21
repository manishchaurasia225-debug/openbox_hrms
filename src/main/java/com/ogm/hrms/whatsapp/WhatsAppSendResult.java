package com.ogm.hrms.whatsapp;

/**
 * Outcome of handing a message to a {@link WhatsAppProvider}.
 *
 * @param accepted          whether the provider accepted the message for delivery
 * @param providerMessageId the provider's message id (used to correlate delivery/read callbacks)
 * @param error             failure reason when {@code accepted} is false; null otherwise
 */
public record WhatsAppSendResult(boolean accepted, String providerMessageId, String error) {

    public static WhatsAppSendResult accepted(String providerMessageId) {
        return new WhatsAppSendResult(true, providerMessageId, null);
    }

    public static WhatsAppSendResult failed(String error) {
        return new WhatsAppSendResult(false, null, error);
    }
}
