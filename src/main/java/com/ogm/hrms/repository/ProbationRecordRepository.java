package com.ogm.hrms.repository;

import com.ogm.hrms.entity.ProbationRecord;
import com.ogm.hrms.enums.ProbationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProbationRecordRepository extends JpaRepository<ProbationRecord, Long> {

    @EntityGraph(attributePaths = "employee")
    Optional<ProbationRecord> findWithEmployeeById(Long id);

    @EntityGraph(attributePaths = "employee")
    Page<ProbationRecord> findByEmployee_Id(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Page<ProbationRecord> findByStatus(ProbationStatus status, Pageable pageable);

    boolean existsByEmployee_IdAndStatusIn(Long employeeId, Collection<ProbationStatus> statuses);

    @EntityGraph(attributePaths = "employee")
    List<ProbationRecord> findByStatusInAndEndDateBetweenOrderByEndDateAsc(Collection<ProbationStatus> statuses,
                                                                           LocalDate from, LocalDate to);
}
