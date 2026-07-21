package com.ogm.hrms.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Default {@link WhatsAppProvider} used when no real provider (e.g. a Meta WhatsApp Business API
 * adapter) is configured. It never calls an external service: it logs the message and returns an
 * accepted result with a synthetic provider id, so delivery/read tracking and the message ledger work
 * end-to-end in dev/test. A production adapter should be supplied as a {@code @Primary}
 * {@link WhatsAppProvider} bean, which then wins injection over this default.
 */
@Component
public class LoggingWhatsAppProvider implements WhatsAppProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingWhatsAppProvider.class);

    @Override
    public WhatsAppSendResult send(String toPhone, String body) {
        String syntheticId = "sim-" + UUID.randomUUID();
        log.info("[whatsapp simulated — no provider configured] to={} id={}\n{}", toPhone, syntheticId, body);
        return WhatsAppSendResult.accepted(syntheticId);
    }

    @Override
    public boolean isLive() {
        return false;
    }
}
