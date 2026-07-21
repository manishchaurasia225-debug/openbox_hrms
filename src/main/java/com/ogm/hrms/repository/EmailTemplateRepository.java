package com.ogm.hrms.repository;

import com.ogm.hrms.entity.EmailTemplate;
import com.ogm.hrms.enums.EmailTemplateCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByIdAndDeletedFalse(Long id);

    Optional<EmailTemplate> findByCodeIgnoreCaseAndDeletedFalse(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<EmailTemplate> findByDeletedFalseOrderByCodeAsc();

    List<EmailTemplate> findByCategoryAndDeletedFalseOrderByCodeAsc(EmailTemplateCategory category);
}
