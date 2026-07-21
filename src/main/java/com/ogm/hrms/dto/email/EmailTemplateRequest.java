package com.ogm.hrms.dto.email;

import com.ogm.hrms.enums.EmailTemplateCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create/replace payload for an email template. */
public record EmailTemplateRequest(
        @NotBlank @Size(max = 100) String code,
        @NotBlank @Size(max = 150) String name,
        @NotNull EmailTemplateCategory category,
        @NotBlank @Size(max = 300) String subject,
        @NotBlank String bodyHtml,
        Boolean active,
        @Size(max = 300) String description
) {
}
