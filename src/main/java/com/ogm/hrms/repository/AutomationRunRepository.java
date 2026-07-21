package com.ogm.hrms.repository;

import com.ogm.hrms.entity.AutomationRun;
import com.ogm.hrms.enums.AutomationRunStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AutomationRunRepository extends JpaRepository<AutomationRun, Long> {

    boolean existsByRule_IdAndRunDateAndStatus(Long ruleId, LocalDate runDate, AutomationRunStatus status);

    @EntityGraph(attributePaths = "rule")
    Page<AutomationRun> findByOrderByCreatedAtDesc(Pageable pageable);
}
