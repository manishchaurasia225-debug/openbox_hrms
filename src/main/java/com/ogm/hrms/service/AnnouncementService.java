package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.communication.AnnouncementRequest;
import com.ogm.hrms.dto.communication.AnnouncementResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Announcement management (RBAC module {@code ANNOUNCEMENT}). */
public interface AnnouncementService {

    AnnouncementResponse create(AnnouncementRequest request);

    AnnouncementResponse publish(Long id);

    PageResponse<AnnouncementResponse> list(Pageable pageable);

    /** The active feed for readers: published and within the publish/expiry window. */
    List<AnnouncementResponse> feed();

    AnnouncementResponse get(Long id);

    AnnouncementResponse update(Long id, AnnouncementRequest request);

    void delete(Long id);
}
