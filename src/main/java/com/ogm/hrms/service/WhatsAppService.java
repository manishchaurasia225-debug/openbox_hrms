package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.whatsapp.RenderedWhatsAppResponse;
import com.ogm.hrms.dto.whatsapp.SendWhatsAppRequest;
import com.ogm.hrms.dto.whatsapp.UpdateMessageStatusRequest;
import com.ogm.hrms.dto.whatsapp.WhatsAppMessageResponse;
import com.ogm.hrms.dto.whatsapp.WhatsAppTemplateRequest;
import com.ogm.hrms.dto.whatsapp.WhatsAppTemplateResponse;
import com.ogm.hrms.enums.WhatsAppTemplateCategory;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * WhatsApp Integration (Module 20): manages WhatsApp message templates, sends messages through a
 * pluggable {@link com.ogm.hrms.whatsapp.WhatsAppProvider}, and tracks the delivery/read lifecycle in
 * a message ledger.
 */
public interface WhatsAppService {

    WhatsAppTemplateResponse createTemplate(WhatsAppTemplateRequest request);

    List<WhatsAppTemplateResponse> listTemplates(WhatsAppTemplateCategory category);

    WhatsAppTemplateResponse getTemplate(Long id);

    WhatsAppTemplateResponse updateTemplate(Long id, WhatsAppTemplateRequest request);

    void deleteTemplate(Long id);

    RenderedWhatsAppResponse previewTemplate(Long id, Map<String, String> variables);

    /** Renders an active template and sends it, recording the outcome in the message ledger. */
    WhatsAppMessageResponse sendByTemplate(Long templateId, SendWhatsAppRequest request);

    PageResponse<WhatsAppMessageResponse> listMessages(Pageable pageable);

    WhatsAppMessageResponse getMessage(Long id);

    /** Advances a message's delivery lifecycle (provider delivery/read callback). */
    WhatsAppMessageResponse updateStatus(Long id, UpdateMessageStatusRequest request);
}
