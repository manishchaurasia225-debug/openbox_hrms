package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.WhatsAppMessageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Ledger of an outbound WhatsApp message and its delivery lifecycle. The {@code providerMessageId}
 * correlates provider delivery/read callbacks back to this row so {@code status} can advance
 * SENT → DELIVERED → READ (or FAILED).
 */
@Entity
@Table(name = "whatsapp_messages",
        indexes = {
                @Index(name = "idx_whatsapp_messages_status", columnList = "status"),
                @Index(name = "idx_whatsapp_messages_provider_id", columnList = "provider_message_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class WhatsAppMessage extends BaseEntity {

    @Column(name = "to_phone", nullable = false, length = 20)
    private String toPhone;

    /** Optional link to the recipient's user account (messages may target a raw phone number). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    /** The template used, if any (null for ad-hoc messages). */
    @Column(name = "template_code", length = 100)
    private String templateCode;

    @Column(name = "body", nullable = false, length = 1000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WhatsAppMessageStatus status;

    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    @Column(name = "failure_reason", length = 300)
    private String failureReason;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;
}
