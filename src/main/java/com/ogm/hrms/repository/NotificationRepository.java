package com.ogm.hrms.repository;

import com.ogm.hrms.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipient_IdAndDeletedFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Page<Notification> findByRecipient_IdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipient_IdAndReadFalseAndDeletedFalse(Long recipientId);

    Optional<Notification> findByIdAndRecipient_IdAndDeletedFalse(Long id, Long recipientId);

    Optional<Notification> findByIdAndDeletedFalse(Long id);

    @Modifying
    @Query("""
            update Notification n set n.read = true, n.readAt = :now
            where n.recipient.id = :recipientId and n.read = false and n.deleted = false
            """)
    int markAllReadForRecipient(@Param("recipientId") Long recipientId, @Param("now") OffsetDateTime now);
}
