package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.document.DocumentDownload;
import com.ogm.hrms.dto.document.DocumentResponse;
import com.ogm.hrms.dto.document.DocumentUploadRequest;
import com.ogm.hrms.entity.Document;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.DocumentRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.security.CurrentAccess;
import com.ogm.hrms.service.DocumentService;
import com.ogm.hrms.storage.StorageProperties;
import com.ogm.hrms.storage.StorageService;
import com.ogm.hrms.storage.VirusScanner;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Default {@link DocumentService}. Validates content type and size, runs the virus-scan hook,
 * persists the binary via the storage backend, and records metadata. Deleting a document soft-deletes
 * the record and removes the stored binary.
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final StorageService storageService;
    private final VirusScanner virusScanner;
    private final StorageProperties storageProperties;
    private final CurrentAccess currentAccess;

    public DocumentServiceImpl(DocumentRepository documentRepository, EmployeeRepository employeeRepository,
                               StorageService storageService, VirusScanner virusScanner,
                               StorageProperties storageProperties, CurrentAccess currentAccess) {
        this.documentRepository = documentRepository;
        this.employeeRepository = employeeRepository;
        this.storageService = storageService;
        this.virusScanner = virusScanner;
        this.storageProperties = storageProperties;
        this.currentAccess = currentAccess;
    }

    /** Deny access to another employee's document when the caller is scoped to their own. */
    private void assertDocumentAccess(Document document) {
        if (!currentAccess.isEmployeeScopeOnly()) {
            return;
        }
        Long own = currentAccess.employeeId();
        Long ownerId = document.getEmployee() != null ? document.getEmployee().getId() : null;
        if (own == null || ownerId == null || !own.equals(ownerId)) {
            throw ApiException.forbidden("You can only access your own documents");
        }
    }

    @Override
    @Transactional
    public DocumentResponse upload(DocumentUploadRequest meta, String originalFilename, String contentType,
                                   byte[] content) {
        if (meta.documentType() == null) {
            throw ApiException.badRequest("documentType is required");
        }
        if (content == null || content.length == 0) {
            throw ApiException.badRequest("The uploaded file is empty");
        }
        if (content.length > storageProperties.maxFileSizeBytes()) {
            throw ApiException.badRequest("The file exceeds the maximum allowed size of "
                    + storageProperties.maxFileSizeBytes() + " bytes");
        }
        if (!storageProperties.allowedContentTypes().isEmpty()
                && (contentType == null || !storageProperties.allowedContentTypes().contains(contentType))) {
            throw ApiException.badRequest("Unsupported file type: " + contentType);
        }

        virusScanner.scan(originalFilename, content);

        Employee employee = null;
        if (meta.employeeId() != null) {
            employee = employeeRepository.findByIdAndDeletedFalse(meta.employeeId())
                    .orElseThrow(() -> ApiException.badRequest("Unknown employee: " + meta.employeeId()));
        }

        String folder = (meta.folder() != null && !meta.folder().isBlank()) ? meta.folder()
                : (employee != null ? "employees/" + employee.getId() : "company");
        String storageKey = storageService.store(folder, originalFilename, content);

        Document document = new Document();
        document.setDocumentType(meta.documentType());
        document.setEmployee(employee);
        document.setTitle((meta.title() != null && !meta.title().isBlank()) ? meta.title().trim() : originalFilename);
        document.setOriginalFilename(originalFilename);
        document.setContentType(contentType);
        document.setSizeBytes((long) content.length);
        document.setStorageKey(storageKey);
        document.setFolder(folder);
        document.setDescription(meta.description());
        document.setExpiryDate(meta.expiryDate());
        return toResponse(documentRepository.save(document));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> list(Long employeeId, Pageable pageable) {
        // Self scope: a standard employee sees only documents linked to their own record.
        if (currentAccess.isEmployeeScopeOnly()) {
            Long own = currentAccess.employeeId();
            if (own == null) {
                return PageResponse.of(org.springframework.data.domain.Page.empty(pageable), this::toResponse);
            }
            employeeId = own;
        }
        var page = (employeeId != null)
                ? documentRepository.findByEmployee_IdAndDeletedFalse(employeeId, pageable)
                : documentRepository.findByDeletedFalse(pageable);
        return PageResponse.of(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse get(Long id) {
        Document document = load(id);
        assertDocumentAccess(document);
        return toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownload download(Long id) {
        Document document = load(id);
        assertDocumentAccess(document);
        return new DocumentDownload(storageService.load(document.getStorageKey()),
                document.getOriginalFilename(), document.getContentType());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Document document = load(id);
        document.setDeleted(true);
        document.setDeletedAt(OffsetDateTime.now());
        storageService.delete(document.getStorageKey());
    }

    private Document load(Long id) {
        return documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    private DocumentResponse toResponse(Document d) {
        return new DocumentResponse(d.getId(), d.getDocumentType(),
                d.getEmployee() != null ? d.getEmployee().getId() : null,
                d.getTitle(), d.getOriginalFilename(), d.getContentType(), d.getSizeBytes(),
                d.getFolder(), d.getDescription(), d.getExpiryDate(), d.getCreatedAt());
    }
}
