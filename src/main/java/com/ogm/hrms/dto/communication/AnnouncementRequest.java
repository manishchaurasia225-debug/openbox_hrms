package com.ogm.hrms.dto.communication;

import com.ogm.hrms.enums.AnnouncementCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/** Create/update payload for an announcement. */
public record AnnouncementRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String body,
        @NotNull AnnouncementCategory category,
        Long targetDepartmentId,
        OffsetDateTime publishAt,
        OffsetDateTime expiresAt,
        Boolean pinned,
        Boolean publishNow
) {
}
