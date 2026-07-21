package com.ogm.hrms.repository;

import com.ogm.hrms.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @EntityGraph(attributePaths = "targetDepartment")
    Page<Announcement> findByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = "targetDepartment")
    Optional<Announcement> findByIdAndDeletedFalse(Long id);

    /** Published announcements currently within their publish/expiry window, pinned first. */
    @Query("""
            select a from Announcement a left join fetch a.targetDepartment
            where a.deleted = false and a.published = true
              and (a.publishAt is null or a.publishAt <= :now)
              and (a.expiresAt is null or a.expiresAt > :now)
            order by a.pinned desc, coalesce(a.publishAt, a.createdAt) desc
            """)
    List<Announcement> findActiveFeed(@Param("now") OffsetDateTime now);
}
