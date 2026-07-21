package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Metadata for a stored document. The binary content lives in the configured storage backend
 * (local filesystem now; S3 pluggable later) addressed by {@code storageKey}. May belong to an
 * {@link Employee} (e.g. offer letter) or to no employee (e.g. company policy). Per project-rules.md,
 * document version history and OCR are out of scope.
 */
@Entity
@Table(name = "documents",
        uniqueConstraints = @UniqueConstraint(name = "uk_documents_storage_key", columnNames = "storage_key"),
        indexes = {
                @Index(name = "idx_documents_employee", columnList = "employee_id"),
                @Index(name = "idx_documents_type", columnList = "document_type")
        })
@Getter
@Setter
@NoArgsConstructor
public class Document extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", foreignKey = @ForeignKey(name = "fk_documents_employee"))
    private Employee employee;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "storage_key", nullable = false, length = 400)
    private String storageKey;

    @Column(name = "folder", length = 200)
    private String folder;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
}
