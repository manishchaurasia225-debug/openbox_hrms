package com.ogm.hrms.repository;

import com.ogm.hrms.entity.WhatsAppTemplate;
import com.ogm.hrms.enums.WhatsAppTemplateCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WhatsAppTemplateRepository extends JpaRepository<WhatsAppTemplate, Long> {

    Optional<WhatsAppTemplate> findByIdAndDeletedFalse(Long id);

    Optional<WhatsAppTemplate> findByCodeIgnoreCaseAndDeletedFalse(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<WhatsAppTemplate> findByDeletedFalseOrderByCodeAsc();

    List<WhatsAppTemplate> findByCategoryAndDeletedFalseOrderByCodeAsc(WhatsAppTemplateCategory category);
}
