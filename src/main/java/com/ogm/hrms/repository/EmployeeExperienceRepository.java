package com.ogm.hrms.repository;

import com.ogm.hrms.entity.EmployeeExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeExperienceRepository extends JpaRepository<EmployeeExperience, Long> {

    List<EmployeeExperience> findByEmployee_IdAndDeletedFalseOrderByIdAsc(Long employeeId);

    Optional<EmployeeExperience> findByIdAndEmployee_IdAndDeletedFalse(Long id, Long employeeId);
}
