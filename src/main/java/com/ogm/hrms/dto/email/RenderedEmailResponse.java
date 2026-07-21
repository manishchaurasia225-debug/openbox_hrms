package com.ogm.hrms.dto.email;

/** The result of rendering a template: the resolved subject and HTML body. */
public record RenderedEmailResponse(
        String code,
        String subject,
        String bodyHtml
) {
}
