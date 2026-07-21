package com.ogm.hrms.repository;

import com.ogm.hrms.entity.LeaveRequest;
import com.ogm.hrms.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @EntityGraph(attributePaths = {"employee", "leaveType"})
    Optional<LeaveRequest> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"employee", "leaveType"})
    Page<LeaveRequest> findByEmployee_Id(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "leaveType"})
    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "leaveType"})
    Page<LeaveRequest> findByEmployee_IdAndStatus(Long employeeId, LeaveStatus status, Pageable pageable);

    /** Approved leaves overlapping the [from, to] window, for the calendar. */
    @EntityGraph(attributePaths = {"employee", "leaveType"})
    List<LeaveRequest> findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            LeaveStatus status, LocalDate to, LocalDate from);

    long countByStatusIn(java.util.Collection<LeaveStatus> statuses);

    long countByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(LeaveStatus status, LocalDate to, LocalDate from);

    @org.springframework.data.jpa.repository.Query(
            "select r from LeaveRequest r join fetch r.employee join fetch r.leaveType "
                    + "where r.deleted = false order by r.fromDate desc")
    List<LeaveRequest> findAllForReport();

    /** Distinct employees with a leave request in the given status, with their user account loaded. */
    @org.springframework.data.jpa.repository.Query(
            "select distinct e from LeaveRequest r join r.employee e left join fetch e.user "
                    + "where r.status = :status and r.deleted = false")
    List<com.ogm.hrms.entity.Employee> findDistinctEmployeesByStatus(LeaveStatus status);
}
