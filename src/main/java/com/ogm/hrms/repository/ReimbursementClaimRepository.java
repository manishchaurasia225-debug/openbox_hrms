package com.ogm.hrms.repository;

import com.ogm.hrms.entity.ReimbursementClaim;
import com.ogm.hrms.enums.ReimbursementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReimbursementClaimRepository extends JpaRepository<ReimbursementClaim, Long> {

    @EntityGraph(attributePaths = {"employee", "billDocument"})
    Optional<ReimbursementClaim> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"employee", "billDocument"})
    Page<ReimbursementClaim> findByEmployee_Id(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "billDocument"})
    Page<ReimbursementClaim> findByStatus(ReimbursementStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "billDocument"})
    Page<ReimbursementClaim> findByEmployee_IdAndStatus(Long employeeId, ReimbursementStatus status, Pageable pageable);
}
