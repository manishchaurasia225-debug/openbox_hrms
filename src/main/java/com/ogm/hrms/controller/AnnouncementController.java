package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.communication.AnnouncementRequest;
import com.ogm.hrms.dto.communication.AnnouncementResponse;
import com.ogm.hrms.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Announcement API, authorized by the {@code ANNOUNCEMENT} RBAC permissions. */
@Tag(name = "Announcements", description = "Create, publish, and browse company-wide announcements.")
@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @Operation(summary = "Create announcement",
            description = "Creates a draft announcement. Requires ANNOUNCEMENT:CREATE.")
    @PostMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENT:CREATE')")
    public ApiResponse<AnnouncementResponse> create(@Valid @RequestBody AnnouncementRequest request,
                                                    HttpServletRequest http) {
        return ApiResponse.success(announcementService.create(request), "Announcement created", http.getRequestURI());
    }

    @Operation(summary = "Publish announcement",
            description = "Publishes an announcement so it becomes visible in the feed. Requires ANNOUNCEMENT:APPROVE.")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT:APPROVE')")
    public ApiResponse<AnnouncementResponse> publish(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(announcementService.publish(id), "Announcement published", http.getRequestURI());
    }

    @Operation(summary = "List announcements",
            description = "Returns a paginated list of all announcements. Requires ANNOUNCEMENT:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENT:VIEW')")
    public ApiResponse<PageResponse<AnnouncementResponse>> list(@PageableDefault(size = 20) Pageable pageable,
                                                                HttpServletRequest http) {
        return ApiResponse.success(announcementService.list(pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get announcement feed",
            description = "Returns the current published announcement feed for the caller. Requires ANNOUNCEMENT:VIEW.")
    @GetMapping("/feed")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT:VIEW')")
    public ApiResponse<List<AnnouncementResponse>> feed(HttpServletRequest http) {
        return ApiResponse.success(announcementService.feed(), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get announcement by id",
            description = "Returns a single announcement by its identifier. Requires ANNOUNCEMENT:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT:VIEW')")
    public ApiResponse<AnnouncementResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(announcementService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update announcement",
            description = "Updates an existing announcement's content. Requires ANNOUNCEMENT:EDIT.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT:EDIT')")
    public ApiResponse<AnnouncementResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody AnnouncementRequest request,
                                                    HttpServletRequest http) {
        return ApiResponse.success(announcementService.update(id, request), "Announcement updated", http.getRequestURI());
    }

    @Operation(summary = "Delete announcement",
            description = "Soft-deletes an announcement. Requires ANNOUNCEMENT:DELETE.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENT:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        announcementService.delete(id);
        return ApiResponse.success(null, "Announcement deleted", http.getRequestURI());
    }
}
