package com.ogm.hrms.repository;

import com.ogm.hrms.entity.LeaveBalance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployee_IdAndLeaveType_IdAndYear(Long employeeId, Long leaveTypeId, int year);

    @EntityGraph(attributePaths = "leaveType")
    List<LeaveBalance> findByEmployee_IdAndYear(Long employeeId, int year);
}
