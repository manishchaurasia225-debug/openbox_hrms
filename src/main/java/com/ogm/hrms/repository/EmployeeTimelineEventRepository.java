package com.ogm.hrms.repository;

import com.ogm.hrms.entity.EmployeeTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeTimelineEventRepository extends JpaRepository<EmployeeTimelineEvent, Long> {

    List<EmployeeTimelineEvent> findByEmployee_IdAndDeletedFalseOrderByEventDateDescIdDesc(Long employeeId);

    Optional<EmployeeTimelineEvent> findByIdAndEmployee_IdAndDeletedFalse(Long id, Long employeeId);

    /** Timeline events of a given type dated on a specific day, with employee + user eagerly loaded. */
    @org.springframework.data.jpa.repository.Query(
            "select ev from EmployeeTimelineEvent ev join fetch ev.employee emp left join fetch emp.user "
                    + "where ev.eventType = :type and ev.eventDate = :date and ev.deleted = false")
    List<EmployeeTimelineEvent> findByEventTypeAndEventDateWithEmployee(
            com.ogm.hrms.enums.TimelineEventType type, java.time.LocalDate date);
}
