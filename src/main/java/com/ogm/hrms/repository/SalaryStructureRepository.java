package com.ogm.hrms.repository;

import com.ogm.hrms.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

    List<SalaryStructure> findByEmployee_IdAndDeletedFalseOrderByEffectiveFromDesc(Long employeeId);

    Optional<SalaryStructure> findTopByEmployee_IdAndEffectiveFromLessThanEqualAndDeletedFalseOrderByEffectiveFromDesc(
            Long employeeId, LocalDate asOf);
}
