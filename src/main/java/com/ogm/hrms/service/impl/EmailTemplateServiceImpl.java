package com.ogm.hrms.service.impl;

import com.ogm.hrms.dto.email.EmailTemplateRequest;
import com.ogm.hrms.dto.email.EmailTemplateResponse;
import com.ogm.hrms.dto.email.RenderedEmailResponse;
import com.ogm.hrms.entity.EmailTemplate;
import com.ogm.hrms.enums.EmailTemplateCategory;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.EmailTemplateRepository;
import com.ogm.hrms.service.EmailService;
import com.ogm.hrms.service.EmailTemplateService;
import com.ogm.hrms.util.PlaceholderRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Default Email Template Engine. Rendering reuses {@link PlaceholderRenderer} ({@code {token}}
 * substitution) so template variables behave identically to automation messages; sending goes
 * through {@link EmailService#sendHtml}, which delivers when SMTP is configured and logs otherwise.
 */
@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateServiceImpl.class);

    private final EmailTemplateRepository templateRepository;
    private final EmailService emailService;

    public EmailTemplateServiceImpl(EmailTemplateRepository templateRepository, EmailService emailService) {
        this.templateRepository = templateRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public EmailTemplateResponse create(EmailTemplateRequest request) {
        if (templateRepository.existsByCodeIgnoreCase(request.code())) {
            throw ApiException.conflict("An email template with code '" + request.code() + "' already exists");
        }
        EmailTemplate template = new EmailTemplate();
        apply(template, request);
        log.info("Email template created: {}", template.getCode());
        return toResponse(templateRepository.save(template));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> list(EmailTemplateCategory category) {
        List<EmailTemplate> templates = category != null
                ? templateRepository.findByCategoryAndDeletedFalseOrderByCodeAsc(category)
                : templateRepository.findByDeletedFalseOrderByCodeAsc();
        return templates.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplateResponse get(Long id) {
        return toResponse(load(id));
    }

    @Override
    @Transactional
    public EmailTemplateResponse update(Long id, EmailTemplateRequest request) {
        EmailTemplate template = load(id);
        if (templateRepository.existsByCodeIgnoreCaseAndIdNot(request.code(), id)) {
            throw ApiException.conflict("An email template with code '" + request.code() + "' already exists");
        }
        apply(template, request);
        log.info("Email template updated: {}", template.getCode());
        return toResponse(templateRepository.save(template));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EmailTemplate template = load(id);
        template.setDeleted(true);
        template.setDeletedAt(java.time.OffsetDateTime.now());
        templateRepository.save(template);
        log.info("Email template deleted: {}", template.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public RenderedEmailResponse preview(Long id, Map<String, String> variables) {
        return render(load(id), variables);
    }

    @Override
    @Transactional(readOnly = true)
    public RenderedEmailResponse renderByCode(String code, Map<String, String> variables) {
        EmailTemplate template = templateRepository.findByCodeIgnoreCaseAndDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "code", code));
        requireActive(template);
        return render(template, variables);
    }

    @Override
    @Transactional(readOnly = true)
    public RenderedEmailResponse send(Long id, String to, Map<String, String> variables) {
        EmailTemplate template = load(id);
        requireActive(template);
        RenderedEmailResponse rendered = render(template, variables);
        emailService.sendHtml(to, rendered.subject(), rendered.bodyHtml());
        log.info("Email template {} sent to {}", template.getCode(), to);
        return rendered;
    }

    private RenderedEmailResponse render(EmailTemplate template, Map<String, String> variables) {
        String subject = PlaceholderRenderer.render(template.getSubject(), variables);
        String body = PlaceholderRenderer.render(template.getBodyHtml(), variables);
        return new RenderedEmailResponse(template.getCode(), subject, body);
    }

    private void requireActive(EmailTemplate template) {
        if (!template.isActive()) {
            throw ApiException.badRequest("Email template '" + template.getCode() + "' is inactive");
        }
    }

    private void apply(EmailTemplate template, EmailTemplateRequest request) {
        template.setCode(request.code());
        template.setName(request.name());
        template.setCategory(request.category());
        template.setSubject(request.subject());
        template.setBodyHtml(request.bodyHtml());
        template.setActive(request.active() == null || request.active());
        template.setDescription(request.description());
    }

    private EmailTemplate load(Long id) {
        return templateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
    }

    private EmailTemplateResponse toResponse(EmailTemplate template) {
        return new EmailTemplateResponse(template.getId(), template.getCode(), template.getName(),
                template.getCategory(), template.getSubject(), template.getBodyHtml(), template.isActive(),
                template.getDescription(), template.getUpdatedAt());
    }
}
