package com.ogm.hrms.repository;

import com.ogm.hrms.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DesignationRepository extends JpaRepository<Designation, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Designation> findByDeletedFalse(Pageable pageable);

    Optional<Designation> findByIdAndDeletedFalse(Long id);

    /** Free-text search over code and name (for global search). */
    @org.springframework.data.jpa.repository.Query(
            "select d from Designation d where d.deleted = false and ("
                    + "lower(d.code) like lower(concat('%', :q, '%')) or "
                    + "lower(d.name) like lower(concat('%', :q, '%'))) order by d.name asc")
    java.util.List<Designation> search(String q, Pageable pageable);
}
