package com.ogm.hrms.dto.whatsapp;

import com.ogm.hrms.enums.WhatsAppTemplateCategory;

import java.time.OffsetDateTime;

/** API view of a WhatsApp template. */
public record WhatsAppTemplateResponse(
        Long id,
        String code,
        String name,
        WhatsAppTemplateCategory category,
        String bodyText,
        boolean active,
        String description,
        OffsetDateTime updatedAt
) {
}
