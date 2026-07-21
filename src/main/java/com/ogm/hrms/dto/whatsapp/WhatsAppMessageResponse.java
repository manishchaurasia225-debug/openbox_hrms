package com.ogm.hrms.dto.whatsapp;

import com.ogm.hrms.enums.WhatsAppMessageStatus;

import java.time.OffsetDateTime;

/** API view of a WhatsApp message ledger entry and its delivery lifecycle. */
public record WhatsAppMessageResponse(
        Long id,
        String toPhone,
        String templateCode,
        String body,
        WhatsAppMessageStatus status,
        String providerMessageId,
        String failureReason,
        OffsetDateTime sentAt,
        OffsetDateTime deliveredAt,
        OffsetDateTime readAt,
        OffsetDateTime createdAt
) {
}
