package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.communication.NotificationResponse;
import com.ogm.hrms.dto.communication.SendNotificationRequest;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Notification center. Center operations act on the caller's own notifications; sending requires
 * {@code NOTIFICATION:CREATE} (system/admin), retry requires {@code NOTIFICATION:ADMIN}.
 */
@Tag(name = "Notifications", description = "In-app notification center: read, dismiss, send, and retry notifications.")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "List my notifications",
            description = "Returns the caller's own notifications, optionally filtered to unread only. Requires NOTIFICATION:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:VIEW')")
    public ApiResponse<PageResponse<NotificationResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(defaultValue = "false") boolean unread,
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(notificationService.listMine(principal, unread, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get unread count",
            description = "Returns the number of unread notifications for the caller. Requires NOTIFICATION:VIEW.")
    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('NOTIFICATION:VIEW')")
    public ApiResponse<Map<String, Long>> unreadCount(@AuthenticationPrincipal AuthenticatedUser principal,
                                                      HttpServletRequest http) {
        return ApiResponse.success(Map.of("unread", notificationService.unreadCount(principal)), "OK",
                http.getRequestURI());
    }

    @Operation(summary = "Mark notification read",
            description = "Marks one of the caller's notifications as read. Requires NOTIFICATION:EDIT.")
    @PostMapping("/{id}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION:EDIT')")
    public ApiResponse<NotificationResponse> markRead(@AuthenticationPrincipal AuthenticatedUser principal,
                                                      @PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(notificationService.markRead(principal, id), "Marked read", http.getRequestURI());
    }

    @Operation(summary = "Mark all read",
            description = "Marks all of the caller's unread notifications as read. Requires NOTIFICATION:EDIT.")
    @PostMapping("/read-all")
    @PreAuthorize("hasAuthority('NOTIFICATION:EDIT')")
    public ApiResponse<Map<String, Integer>> markAllRead(@AuthenticationPrincipal AuthenticatedUser principal,
                                                         HttpServletRequest http) {
        return ApiResponse.success(Map.of("updated", notificationService.markAllRead(principal)),
                "All marked read", http.getRequestURI());
    }

    @Operation(summary = "Delete notification",
            description = "Deletes one of the caller's own notifications. Requires NOTIFICATION:DELETE.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION:DELETE')")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long id,
                                    HttpServletRequest http) {
        notificationService.delete(principal, id);
        return ApiResponse.success(null, "Notification deleted", http.getRequestURI());
    }

    @Operation(summary = "Send notification",
            description = "Sends a notification to a target recipient (system/admin use). Requires NOTIFICATION:CREATE.")
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('NOTIFICATION:CREATE')")
    public ApiResponse<NotificationResponse> send(@Valid @RequestBody SendNotificationRequest request,
                                                  HttpServletRequest http) {
        return ApiResponse.success(notificationService.send(request), "Notification sent", http.getRequestURI());
    }

    @Operation(summary = "Retry notification delivery",
            description = "Re-attempts delivery of a previously failed notification. Requires NOTIFICATION:ADMIN.")
    @PostMapping("/{id}/retry")
    @PreAuthorize("hasAuthority('NOTIFICATION:ADMIN')")
    public ApiResponse<NotificationResponse> retry(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(notificationService.retry(id), "Notification retried", http.getRequestURI());
    }
}
