package com.ogm.hrms.repository;

import com.ogm.hrms.entity.Payslip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    boolean existsByEmployee_IdAndPeriodYearAndPeriodMonthAndDeletedFalse(Long employeeId, int year, int month);

    long countByEmployee_IdAndDeletedFalse(Long employeeId);

    @EntityGraph(attributePaths = "employee")
    Page<Payslip> findByEmployee_IdAndDeletedFalseOrderByPeriodYearDescPeriodMonthDesc(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Optional<Payslip> findByIdAndDeletedFalse(Long id);
}
