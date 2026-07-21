package com.ogm.hrms.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/** Request to render a template with the given variables and send it to a recipient. */
public record SendTemplateEmailRequest(
        @NotBlank @Email String to,
        Map<String, String> variables
) {
}
