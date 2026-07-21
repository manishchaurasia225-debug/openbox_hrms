package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.document.DocumentDownload;
import com.ogm.hrms.dto.document.DocumentResponse;
import com.ogm.hrms.dto.document.DocumentUploadRequest;
import com.ogm.hrms.enums.DocumentType;
import com.ogm.hrms.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Document management API. Uploads are multipart; downloads/previews stream binary content (not
 * wrapped in the ApiResponse envelope). Reads require {@code DOCUMENT:VIEW}, uploads
 * {@code DOCUMENT:CREATE}, deletes {@code DOCUMENT:DELETE}.
 */
@Tag(name = "Documents", description = "Upload, list, preview, download, and delete employee and company documents.")
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Upload document", description = "Uploads a document file via multipart form data along with its metadata. Requires DOCUMENT:CREATE.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT:CREATE')")
    public ApiResponse<DocumentResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "expiryDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            HttpServletRequest http) throws IOException {
        DocumentUploadRequest meta = new DocumentUploadRequest(documentType, employeeId, title, folder,
                description, expiryDate);
        DocumentResponse created = documentService.upload(meta, file.getOriginalFilename(),
                file.getContentType(), file.getBytes());
        return ApiResponse.success(created, "Document uploaded", http.getRequestURI());
    }

    @Operation(summary = "List documents", description = "Returns a paginated list of documents, optionally filtered by employee. Requires DOCUMENT:VIEW.")
    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT:VIEW')")
    public ApiResponse<PageResponse<DocumentResponse>> list(
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @PageableDefault(size = 20) Pageable pageable, HttpServletRequest http) {
        return ApiResponse.success(documentService.list(employeeId, pageable), "OK", http.getRequestURI());
    }

    @Operation(summary = "Get document metadata", description = "Returns the metadata for a single document by its identifier. Requires DOCUMENT:VIEW.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:VIEW')")
    public ApiResponse<DocumentResponse> get(@PathVariable Long id, HttpServletRequest http) {
        return ApiResponse.success(documentService.get(id), "OK", http.getRequestURI());
    }

    @Operation(summary = "Download document", description = "Streams the document binary content as an attachment for download. Requires DOCUMENT:VIEW.")
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('DOCUMENT:VIEW')")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return stream(documentService.download(id), true);
    }

    @Operation(summary = "Preview document", description = "Streams the document binary content inline for in-browser preview. Requires DOCUMENT:VIEW.")
    @GetMapping("/{id}/preview")
    @PreAuthorize("hasAuthority('DOCUMENT:VIEW')")
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        return stream(documentService.download(id), false);
    }

    @Operation(summary = "Delete document", description = "Deletes a document by its identifier. Requires DOCUMENT:DELETE.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT:DELETE')")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        documentService.delete(id);
        return ApiResponse.success(null, "Document deleted", http.getRequestURI());
    }

    private ResponseEntity<Resource> stream(DocumentDownload download, boolean attachment) {
        String contentType = download.contentType() != null ? download.contentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String disposition = (attachment ? "attachment" : "inline")
                + "; filename=\"" + download.filename() + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(MediaType.parseMediaType(contentType))
                .body(download.resource());
    }
}
