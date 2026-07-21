package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.communication.NotificationResponse;
import com.ogm.hrms.dto.communication.SendNotificationRequest;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.NotificationChannel;
import com.ogm.hrms.security.AuthenticatedUser;
import org.springframework.data.domain.Pageable;

/**
 * Notification center (RBAC module {@code NOTIFICATION}). {@link #notify} is the internal producer
 * API other modules call to raise a notification; the rest is the user-facing center.
 */
public interface NotificationService {

    /** Raise a notification for a recipient (no-op if recipient is null). Called by other modules. */
    void notify(User recipient, NotificationChannel channel, String title, String message,
                String referenceType, Long referenceId);

    NotificationResponse send(SendNotificationRequest request);

    PageResponse<NotificationResponse> listMine(AuthenticatedUser principal, boolean unreadOnly, Pageable pageable);

    long unreadCount(AuthenticatedUser principal);

    NotificationResponse markRead(AuthenticatedUser principal, Long id);

    int markAllRead(AuthenticatedUser principal);

    void delete(AuthenticatedUser principal, Long id);

    NotificationResponse retry(Long id);
}
