package com.ogm.hrms.repository;

import com.ogm.hrms.entity.WhatsAppMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WhatsAppMessageRepository extends JpaRepository<WhatsAppMessage, Long> {

    Optional<WhatsAppMessage> findByIdAndDeletedFalse(Long id);

    Optional<WhatsAppMessage> findByProviderMessageId(String providerMessageId);

    Page<WhatsAppMessage> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
}
