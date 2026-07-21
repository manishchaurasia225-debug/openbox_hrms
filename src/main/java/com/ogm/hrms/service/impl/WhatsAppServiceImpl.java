package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.whatsapp.RenderedWhatsAppResponse;
import com.ogm.hrms.dto.whatsapp.SendWhatsAppRequest;
import com.ogm.hrms.dto.whatsapp.UpdateMessageStatusRequest;
import com.ogm.hrms.dto.whatsapp.WhatsAppMessageResponse;
import com.ogm.hrms.dto.whatsapp.WhatsAppTemplateRequest;
import com.ogm.hrms.dto.whatsapp.WhatsAppTemplateResponse;
import com.ogm.hrms.entity.WhatsAppMessage;
import com.ogm.hrms.entity.WhatsAppTemplate;
import com.ogm.hrms.enums.WhatsAppMessageStatus;
import com.ogm.hrms.enums.WhatsAppTemplateCategory;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.repository.WhatsAppMessageRepository;
import com.ogm.hrms.repository.WhatsAppTemplateRepository;
import com.ogm.hrms.service.WhatsAppService;
import com.ogm.hrms.util.PlaceholderRenderer;
import com.ogm.hrms.whatsapp.WhatsAppProvider;
import com.ogm.hrms.whatsapp.WhatsAppSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Default WhatsApp Integration. Templates behave like the email engine (code-addressed,
 * {@code {placeholder}} variables); sends go through the injected {@link WhatsAppProvider} (a real
 * Meta adapter in production, the logging provider otherwise). Every send is recorded in the ledger,
 * and provider callbacks advance the delivery/read status.
 */
@Service
public class WhatsAppServiceImpl implements WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppServiceImpl.class);

    private final WhatsAppTemplateRepository templateRepository;
    private final WhatsAppMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final WhatsAppProvider provider;

    public WhatsAppServiceImpl(WhatsAppTemplateRepository templateRepository,
                               WhatsAppMessageRepository messageRepository, UserRepository userRepository,
                               WhatsAppProvider provider) {
        this.templateRepository = templateRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.provider = provider;
    }

    // --- Templates -------------------------------------------------------------------------------

    @Override
    @Transactional
    public WhatsAppTemplateResponse createTemplate(WhatsAppTemplateRequest request) {
        if (templateRepository.existsByCodeIgnoreCase(request.code())) {
            throw ApiException.conflict("A WhatsApp template with code '" + request.code() + "' already exists");
        }
        WhatsAppTemplate template = new WhatsAppTemplate();
        applyTemplate(template, request);
        log.info("WhatsApp template created: {}", template.getCode());
        return toTemplateResponse(templateRepository.save(template));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WhatsAppTemplateResponse> listTemplates(WhatsAppTemplateCategory category) {
        List<WhatsAppTemplate> templates = category != null
                ? templateRepository.findByCategoryAndDeletedFalseOrderByCodeAsc(category)
                : templateRepository.findByDeletedFalseOrderByCodeAsc();
        return templates.stream().map(this::toTemplateResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WhatsAppTemplateResponse getTemplate(Long id) {
        return toTemplateResponse(loadTemplate(id));
    }

    @Override
    @Transactional
    public WhatsAppTemplateResponse updateTemplate(Long id, WhatsAppTemplateRequest request) {
        WhatsAppTemplate template = loadTemplate(id);
        if (templateRepository.existsByCodeIgnoreCaseAndIdNot(request.code(), id)) {
            throw ApiException.conflict("A WhatsApp template with code '" + request.code() + "' already exists");
        }
        applyTemplate(template, request);
        log.info("WhatsApp template updated: {}", template.getCode());
        return toTemplateResponse(templateRepository.save(template));
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        WhatsAppTemplate template = loadTemplate(id);
        template.setDeleted(true);
        template.setDeletedAt(OffsetDateTime.now());
        templateRepository.save(template);
        log.info("WhatsApp template deleted: {}", template.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public RenderedWhatsAppResponse previewTemplate(Long id, Map<String, String> variables) {
        WhatsAppTemplate template = loadTemplate(id);
        return new RenderedWhatsAppResponse(template.getCode(),
                PlaceholderRenderer.render(template.getBodyText(), variables));
    }

    // --- Messages --------------------------------------------------------------------------------

    @Override
    @Transactional
    public WhatsAppMessageResponse sendByTemplate(Long templateId, SendWhatsAppRequest request) {
        WhatsAppTemplate template = loadTemplate(templateId);
        if (!template.isActive()) {
            throw ApiException.badRequest("WhatsApp template '" + template.getCode() + "' is inactive");
        }
        String body = PlaceholderRenderer.render(template.getBodyText(), request.variables());

        WhatsAppMessage message = new WhatsAppMessage();
        message.setToPhone(request.toPhone());
        message.setTemplateCode(template.getCode());
        message.setBody(body);
        message.setStatus(WhatsAppMessageStatus.QUEUED);
        if (request.recipientUserId() != null) {
            userRepository.findById(request.recipientUserId()).ifPresent(message::setRecipient);
        }

        WhatsAppSendResult result = provider.send(request.toPhone(), body);
        if (result.accepted()) {
            message.setStatus(WhatsAppMessageStatus.SENT);
            message.setProviderMessageId(result.providerMessageId());
            message.setSentAt(OffsetDateTime.now());
        } else {
            message.setStatus(WhatsAppMessageStatus.FAILED);
            message.setFailureReason(result.error());
        }
        log.info("WhatsApp message to {} via template {} -> {}", request.toPhone(), template.getCode(),
                message.getStatus());
        return toMessageResponse(messageRepository.save(message));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WhatsAppMessageResponse> listMessages(Pageable pageable) {
        return PageResponse.of(messageRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable),
                this::toMessageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public WhatsAppMessageResponse getMessage(Long id) {
        return toMessageResponse(loadMessage(id));
    }

    @Override
    @Transactional
    public WhatsAppMessageResponse updateStatus(Long id, UpdateMessageStatusRequest request) {
        WhatsAppMessage message = loadMessage(id);
        OffsetDateTime now = OffsetDateTime.now();
        switch (request.status()) {
            case SENT -> {
                if (message.getSentAt() == null) {
                    message.setSentAt(now);
                }
            }
            case DELIVERED -> {
                ensureSent(message, now);
                message.setDeliveredAt(now);
            }
            case READ -> {
                ensureSent(message, now);
                if (message.getDeliveredAt() == null) {
                    message.setDeliveredAt(now);
                }
                message.setReadAt(now);
            }
            case FAILED -> message.setFailureReason(request.failureReason());
            case QUEUED -> throw ApiException.badRequest("Cannot revert a message to QUEUED");
        }
        message.setStatus(request.status());
        log.info("WhatsApp message {} status -> {}", id, request.status());
        return toMessageResponse(messageRepository.save(message));
    }

    private void ensureSent(WhatsAppMessage message, OffsetDateTime now) {
        if (message.getSentAt() == null) {
            message.setSentAt(now);
        }
    }

    // --- helpers ---------------------------------------------------------------------------------

    private void applyTemplate(WhatsAppTemplate template, WhatsAppTemplateRequest request) {
        template.setCode(request.code());
        template.setName(request.name());
        template.setCategory(request.category());
        template.setBodyText(request.bodyText());
        template.setActive(request.active() == null || request.active());
        template.setDescription(request.description());
    }

    private WhatsAppTemplate loadTemplate(Long id) {
        return templateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WhatsAppTemplate", "id", id));
    }

    private WhatsAppMessage loadMessage(Long id) {
        return messageRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("WhatsAppMessage", "id", id));
    }

    private WhatsAppTemplateResponse toTemplateResponse(WhatsAppTemplate t) {
        return new WhatsAppTemplateResponse(t.getId(), t.getCode(), t.getName(), t.getCategory(),
                t.getBodyText(), t.isActive(), t.getDescription(), t.getUpdatedAt());
    }

    private WhatsAppMessageResponse toMessageResponse(WhatsAppMessage m) {
        return new WhatsAppMessageResponse(m.getId(), m.getToPhone(), m.getTemplateCode(), m.getBody(),
                m.getStatus(), m.getProviderMessageId(), m.getFailureReason(), m.getSentAt(), m.getDeliveredAt(),
                m.getReadAt(), m.getCreatedAt());
    }
}
