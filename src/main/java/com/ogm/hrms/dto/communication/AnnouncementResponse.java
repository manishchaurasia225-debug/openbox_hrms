package com.ogm.hrms.dto.communication;

import com.ogm.hrms.enums.AnnouncementCategory;

import java.time.OffsetDateTime;

/** Announcement view. */
public record AnnouncementResponse(
        Long id,
        String title,
        String body,
        AnnouncementCategory category,
        Long targetDepartmentId,
        String targetDepartmentName,
        OffsetDateTime publishAt,
        OffsetDateTime expiresAt,
        boolean published,
        boolean pinned,
        OffsetDateTime createdAt
) {
}
