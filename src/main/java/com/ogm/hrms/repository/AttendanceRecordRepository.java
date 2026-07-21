package com.ogm.hrms.repository;

import com.ogm.hrms.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEmployee_IdAndAttendanceDate(Long employeeId, LocalDate date);

    boolean existsByEmployee_IdAndAttendanceDate(Long employeeId, LocalDate date);

    @EntityGraph(attributePaths = "employee")
    Optional<AttendanceRecord> findWithEmployeeById(Long id);

    @EntityGraph(attributePaths = "employee")
    Page<AttendanceRecord> findByEmployee_IdAndAttendanceDateBetween(Long employeeId, LocalDate from, LocalDate to,
                                                                     Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Page<AttendanceRecord> findByAttendanceDate(LocalDate date, Pageable pageable);

    List<AttendanceRecord> findByEmployee_IdAndAttendanceDateBetween(Long employeeId, LocalDate from, LocalDate to);

    long countByAttendanceDateAndAttendanceTypeIn(LocalDate date, java.util.Collection<com.ogm.hrms.enums.AttendanceType> types);

    long countByApprovalStatus(com.ogm.hrms.enums.ApprovalStatus approvalStatus);

    @org.springframework.data.jpa.repository.Query(
            "select a from AttendanceRecord a join fetch a.employee "
                    + "where a.attendanceDate between :from and :to order by a.attendanceDate desc")
    List<AttendanceRecord> findForReport(LocalDate from, LocalDate to);
}
