package com.ogm.hrms.dto.communication;

import com.ogm.hrms.enums.NotificationChannel;
import com.ogm.hrms.enums.NotificationStatus;

import java.time.OffsetDateTime;

/** Notification view. */
public record NotificationResponse(
        Long id,
        NotificationChannel channel,
        String title,
        String message,
        NotificationStatus status,
        boolean read,
        OffsetDateTime readAt,
        String referenceType,
        Long referenceId,
        OffsetDateTime createdAt
) {
}
