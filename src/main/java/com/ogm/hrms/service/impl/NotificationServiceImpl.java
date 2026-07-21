package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.communication.NotificationResponse;
import com.ogm.hrms.dto.communication.SendNotificationRequest;
import com.ogm.hrms.entity.Notification;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.NotificationChannel;
import com.ogm.hrms.enums.NotificationStatus;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.NotificationRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Default {@link NotificationService}. In-app notifications are marked SENT on creation; other
 * channels record status for later delivery/retry (email/WhatsApp integrations land in Phase 8).
 * Center operations are scoped to the caller's own notifications.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void notify(User recipient, NotificationChannel channel, String title, String message,
                       String referenceType, Long referenceId) {
        if (recipient == null) {
            return;
        }
        create(recipient, channel != null ? channel : NotificationChannel.IN_APP, title, message,
                referenceType, referenceId);
    }

    @Override
    @Transactional
    public NotificationResponse send(SendNotificationRequest request) {
        User recipient = userRepository.findById(request.userId())
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> ApiException.badRequest("Unknown user: " + request.userId()));
        Notification notification = create(recipient,
                request.channel() != null ? request.channel() : NotificationChannel.IN_APP,
                request.title(), request.message(), null, null);
        return toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listMine(AuthenticatedUser principal, boolean unreadOnly,
                                                       Pageable pageable) {
        var page = unreadOnly
                ? notificationRepository.findByRecipient_IdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(principal.id(), pageable)
                : notificationRepository.findByRecipient_IdAndDeletedFalseOrderByCreatedAtDesc(principal.id(), pageable);
        return PageResponse.of(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(AuthenticatedUser principal) {
        return notificationRepository.countByRecipient_IdAndReadFalseAndDeletedFalse(principal.id());
    }

    @Override
    @Transactional
    public NotificationResponse markRead(AuthenticatedUser principal, Long id) {
        Notification notification = notificationRepository.findByIdAndRecipient_IdAndDeletedFalse(id, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(OffsetDateTime.now());
        }
        return toResponse(notification);
    }

    @Override
    @Transactional
    public int markAllRead(AuthenticatedUser principal) {
        return notificationRepository.markAllReadForRecipient(principal.id(), OffsetDateTime.now());
    }

    @Override
    @Transactional
    public void delete(AuthenticatedUser principal, Long id) {
        Notification notification = notificationRepository.findByIdAndRecipient_IdAndDeletedFalse(id, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        notification.setDeleted(true);
        notification.setDeletedAt(OffsetDateTime.now());
    }

    @Override
    @Transactional
    public NotificationResponse retry(Long id) {
        Notification notification = notificationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw ApiException.badRequest("Only failed notifications can be retried");
        }
        // Re-attempt delivery. Real channel integrations (email/WhatsApp) land in Phase 8; in-app
        // and stubbed channels are marked delivered.
        notification.setStatus(NotificationStatus.SENT);
        notification.setFailureReason(null);
        return toResponse(notification);
    }

    private Notification create(User recipient, NotificationChannel channel, String title, String message,
                                String referenceType, Long referenceId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setChannel(channel);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.SENT);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        return notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getChannel(), n.getTitle(), n.getMessage(), n.getStatus(),
                n.isRead(), n.getReadAt(), n.getReferenceType(), n.getReferenceId(), n.getCreatedAt());
    }
}
