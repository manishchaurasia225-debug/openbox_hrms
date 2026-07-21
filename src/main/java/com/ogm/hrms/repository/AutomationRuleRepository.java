package com.ogm.hrms.repository;

import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.enums.AutomationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    Optional<AutomationRule> findByType(AutomationType type);

    boolean existsByType(AutomationType type);

    List<AutomationRule> findByDeletedFalseOrderByTypeAsc();

    List<AutomationRule> findByEnabledTrueAndDeletedFalse();
}
