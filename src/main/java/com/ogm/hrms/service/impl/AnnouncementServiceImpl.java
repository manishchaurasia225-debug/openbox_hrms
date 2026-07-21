package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.communication.AnnouncementRequest;
import com.ogm.hrms.dto.communication.AnnouncementResponse;
import com.ogm.hrms.entity.Announcement;
import com.ogm.hrms.entity.Department;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.AnnouncementRepository;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.service.AnnouncementService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/** Default {@link AnnouncementService}: draft/publish lifecycle and an active reader feed. */
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final DepartmentRepository departmentRepository;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository,
                                   DepartmentRepository departmentRepository) {
        this.announcementRepository = announcementRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public AnnouncementResponse create(AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        apply(announcement, request);
        if (Boolean.TRUE.equals(request.publishNow())) {
            announcement.setPublished(true);
            if (announcement.getPublishAt() == null) {
                announcement.setPublishAt(OffsetDateTime.now());
            }
        }
        return toResponse(announcementRepository.save(announcement));
    }

    @Override
    @Transactional
    public AnnouncementResponse publish(Long id) {
        Announcement announcement = load(id);
        announcement.setPublished(true);
        if (announcement.getPublishAt() == null) {
            announcement.setPublishAt(OffsetDateTime.now());
        }
        return toResponse(announcement);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AnnouncementResponse> list(Pageable pageable) {
        return PageResponse.of(announcementRepository.findByDeletedFalse(pageable), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> feed() {
        return announcementRepository.findActiveFeed(OffsetDateTime.now()).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementResponse get(Long id) {
        return toResponse(load(id));
    }

    @Override
    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest request) {
        Announcement announcement = load(id);
        apply(announcement, request);
        return toResponse(announcement);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Announcement announcement = load(id);
        announcement.setDeleted(true);
        announcement.setDeletedAt(OffsetDateTime.now());
    }

    private Announcement load(Long id) {
        return announcementRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
    }

    private void apply(Announcement announcement, AnnouncementRequest request) {
        announcement.setTitle(request.title().trim());
        announcement.setBody(request.body());
        announcement.setCategory(request.category());
        announcement.setPublishAt(request.publishAt());
        announcement.setExpiresAt(request.expiresAt());
        announcement.setPinned(request.pinned() != null && request.pinned());
        if (request.targetDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndDeletedFalse(request.targetDepartmentId())
                    .orElseThrow(() -> ApiException.badRequest("Unknown department: " + request.targetDepartmentId()));
            announcement.setTargetDepartment(department);
        } else {
            announcement.setTargetDepartment(null);
        }
    }

    private AnnouncementResponse toResponse(Announcement a) {
        Department d = a.getTargetDepartment();
        return new AnnouncementResponse(a.getId(), a.getTitle(), a.getBody(), a.getCategory(),
                d != null ? d.getId() : null, d != null ? d.getName() : null,
                a.getPublishAt(), a.getExpiresAt(), a.isPublished(), a.isPinned(), a.getCreatedAt());
    }
}
