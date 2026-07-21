package com.ogm.hrms.dto.whatsapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Request to render a WhatsApp template and send it. The phone number must be in E.164 form
 * (e.g. {@code +14155552671}); an optional recipient user id links the message to an account.
 */
public record SendWhatsAppRequest(
        @NotBlank @Pattern(regexp = "\\+[1-9]\\d{7,14}", message = "Phone must be E.164, e.g. +14155552671")
        @Size(max = 20) String toPhone,

        Long recipientUserId,

        Map<String, String> variables
) {
}
