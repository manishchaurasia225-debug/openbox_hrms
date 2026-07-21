package com.ogm.hrms.service;

import com.ogm.hrms.dto.email.EmailTemplateRequest;
import com.ogm.hrms.dto.email.EmailTemplateResponse;
import com.ogm.hrms.dto.email.RenderedEmailResponse;
import com.ogm.hrms.enums.EmailTemplateCategory;

import java.util.List;
import java.util.Map;

/**
 * Email Template Engine (Module 19): manages reusable HTML templates with categories and dynamic
 * {@code {placeholder}} variables, and renders/sends them. Templates are addressed by a stable code
 * so other modules can render by code without hardcoding wording.
 */
public interface EmailTemplateService {

    EmailTemplateResponse create(EmailTemplateRequest request);

    /** Lists templates, optionally filtered by {@code category} (null = all). */
    List<EmailTemplateResponse> list(EmailTemplateCategory category);

    EmailTemplateResponse get(Long id);

    EmailTemplateResponse update(Long id, EmailTemplateRequest request);

    void delete(Long id);

    /** Renders a template (by id) with the given variables for preview; allowed on inactive templates. */
    RenderedEmailResponse preview(Long id, Map<String, String> variables);

    /** Renders an active template by its code — the programmatic entry point for other modules. */
    RenderedEmailResponse renderByCode(String code, Map<String, String> variables);

    /** Renders an active template (by id) and sends it to {@code to} via {@link EmailService}. */
    RenderedEmailResponse send(Long id, String to, Map<String, String> variables);
}
