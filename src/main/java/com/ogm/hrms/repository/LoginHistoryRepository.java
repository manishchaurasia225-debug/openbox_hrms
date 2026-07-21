package com.ogm.hrms.repository;

import com.ogm.hrms.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    Page<LoginHistory> findByEmailIgnoreCaseOrderByCreatedAtDesc(String email, Pageable pageable);

    Page<LoginHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
