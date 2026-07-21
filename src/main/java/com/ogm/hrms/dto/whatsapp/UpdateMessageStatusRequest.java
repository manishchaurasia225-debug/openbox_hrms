package com.ogm.hrms.dto.whatsapp;

import com.ogm.hrms.enums.WhatsAppMessageStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Records a provider delivery/read callback for a message. In production this is driven by a Meta
 * webhook; here it is an authenticated administrative endpoint that advances the message lifecycle.
 */
public record UpdateMessageStatusRequest(
        @NotNull WhatsAppMessageStatus status,
        String failureReason
) {
}
