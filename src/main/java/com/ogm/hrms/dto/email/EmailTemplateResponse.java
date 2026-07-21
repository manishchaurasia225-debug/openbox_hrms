package com.ogm.hrms.dto.email;

import com.ogm.hrms.enums.EmailTemplateCategory;

import java.time.OffsetDateTime;

/** API view of an email template. */
public record EmailTemplateResponse(
        Long id,
        String code,
        String name,
        EmailTemplateCategory category,
        String subject,
        String bodyHtml,
        boolean active,
        String description,
        OffsetDateTime updatedAt
) {
}
