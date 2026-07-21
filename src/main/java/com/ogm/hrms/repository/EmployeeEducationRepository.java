package com.ogm.hrms.repository;

import com.ogm.hrms.entity.EmployeeEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeEducationRepository extends JpaRepository<EmployeeEducation, Long> {

    List<EmployeeEducation> findByEmployee_IdAndDeletedFalseOrderByIdAsc(Long employeeId);

    Optional<EmployeeEducation> findByIdAndEmployee_IdAndDeletedFalse(Long id, Long employeeId);
}
