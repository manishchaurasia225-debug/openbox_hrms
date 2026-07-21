package com.ogm.hrms.dto.whatsapp;

/** The result of rendering a WhatsApp template: the resolved message body. */
public record RenderedWhatsAppResponse(
        String code,
        String body
) {
}
