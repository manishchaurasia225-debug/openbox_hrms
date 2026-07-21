package com.ogm.hrms.repository;

import com.ogm.hrms.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    List<FamilyMember> findByEmployee_IdAndDeletedFalseOrderByIdAsc(Long employeeId);

    Optional<FamilyMember> findByIdAndEmployee_IdAndDeletedFalse(Long id, Long employeeId);
}
