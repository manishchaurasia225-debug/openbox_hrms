package com.ogm.hrms.repository;

import com.ogm.hrms.entity.LifecycleTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LifecycleTaskRepository extends JpaRepository<LifecycleTask, Long> {

    Optional<LifecycleTask> findByIdAndLifecycleCase_IdAndDeletedFalse(Long id, Long caseId);
}
