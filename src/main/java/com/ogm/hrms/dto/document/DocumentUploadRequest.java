package com.ogm.hrms.dto.document;

import com.ogm.hrms.enums.DocumentType;

import java.time.LocalDate;

/** Metadata accompanying a document upload (the binary arrives as a multipart file part). */
public record DocumentUploadRequest(
        DocumentType documentType,
        Long employeeId,
        String title,
        String folder,
        String description,
        LocalDate expiryDate
) {
}
