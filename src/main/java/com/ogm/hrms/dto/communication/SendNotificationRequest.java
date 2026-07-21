package com.ogm.hrms.dto.communication;

import com.ogm.hrms.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Admin/system request to send a notification to a specific user. */
public record SendNotificationRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String message,
        NotificationChannel channel
) {
}
