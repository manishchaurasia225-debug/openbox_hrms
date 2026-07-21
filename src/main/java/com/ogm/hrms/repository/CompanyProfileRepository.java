package com.ogm.hrms.repository;

import com.ogm.hrms.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {

    /** The single company profile row (single-company platform). */
    Optional<CompanyProfile> findTopByOrderByIdAsc();
}
