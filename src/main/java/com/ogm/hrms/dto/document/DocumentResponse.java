package com.ogm.hrms.dto.document;

import com.ogm.hrms.enums.DocumentType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Document metadata view returned by the API. */
public record DocumentResponse(
        Long id,
        DocumentType documentType,
        Long employeeId,
        String title,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        String folder,
        String description,
        LocalDate expiryDate,
        OffsetDateTime createdAt
) {
}
