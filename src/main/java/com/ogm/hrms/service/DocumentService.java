package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.document.DocumentDownload;
import com.ogm.hrms.dto.document.DocumentResponse;
import com.ogm.hrms.dto.document.DocumentUploadRequest;
import org.springframework.data.domain.Pageable;

/** Document management (RBAC module {@code DOCUMENT}). */
public interface DocumentService {

    DocumentResponse upload(DocumentUploadRequest meta, String originalFilename, String contentType, byte[] content);

    PageResponse<DocumentResponse> list(Long employeeId, Pageable pageable);

    DocumentResponse get(Long id);

    DocumentDownload download(Long id);

    void delete(Long id);
}
