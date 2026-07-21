package com.ogm.hrms.repository;

import com.ogm.hrms.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = "employee")
    Page<Document> findByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Page<Document> findByEmployee_IdAndDeletedFalse(Long employeeId, Pageable pageable);

    Optional<Document> findByIdAndDeletedFalse(Long id);

    /** All (employeeId, documentType) pairs for employee-owned documents, for missing-document analysis. */
    @org.springframework.data.jpa.repository.Query(
            "select d.employee.id, d.documentType from Document d "
                    + "where d.deleted = false and d.employee is not null")
    java.util.List<Object[]> findEmployeeDocumentTypes();
}
