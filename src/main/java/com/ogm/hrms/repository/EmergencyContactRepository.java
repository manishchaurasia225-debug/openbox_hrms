package com.ogm.hrms.repository;

import com.ogm.hrms.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByEmployee_IdAndDeletedFalseOrderByIdAsc(Long employeeId);

    Optional<EmergencyContact> findByIdAndEmployee_IdAndDeletedFalse(Long id, Long employeeId);
}
