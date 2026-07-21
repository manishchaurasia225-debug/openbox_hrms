package com.ogm.hrms.repository;

import com.ogm.hrms.entity.LifecycleCase;
import com.ogm.hrms.enums.LifecycleStatus;
import com.ogm.hrms.enums.LifecycleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LifecycleCaseRepository extends JpaRepository<LifecycleCase, Long> {

    @EntityGraph(attributePaths = {"employee", "tasks"})
    Optional<LifecycleCase> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = "employee")
    Page<LifecycleCase> findByEmployee_Id(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Page<LifecycleCase> findByType(LifecycleType type, Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Page<LifecycleCase> findByStatus(LifecycleStatus status, Pageable pageable);
}
