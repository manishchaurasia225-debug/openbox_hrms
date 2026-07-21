package com.ogm.hrms.dto.whatsapp;

import com.ogm.hrms.enums.WhatsAppTemplateCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create/replace payload for a WhatsApp template. */
public record WhatsAppTemplateRequest(
        @NotBlank @Size(max = 100) String code,
        @NotBlank @Size(max = 150) String name,
        @NotNull WhatsAppTemplateCategory category,
        @NotBlank @Size(max = 1000) String bodyText,
        Boolean active,
        @Size(max = 300) String description
) {
}
