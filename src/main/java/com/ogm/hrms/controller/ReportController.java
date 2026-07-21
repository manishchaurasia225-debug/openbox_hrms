package com.ogm.hrms.controller;

import com.ogm.hrms.enums.ReportFormat;
import com.ogm.hrms.enums.ReportType;
import com.ogm.hrms.report.ReportFile;
import com.ogm.hrms.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Report export API. {@code GET /api/v1/reports/{type}?format=CSV|EXCEL|PDF} streams the rendered
 * report. Authorized by {@code REPORT:EXPORT}.
 */
@Tag(name = "Reports", description = "Generate and export operational reports in CSV, Excel, or PDF format.")
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Export report", description = "Generates the requested report type over an optional date range and streams it in the chosen format (CSV, Excel, or PDF). Requires REPORT:EXPORT.")
    @GetMapping("/{type}")
    @PreAuthorize("hasAuthority('REPORT:EXPORT')")
    public ResponseEntity<byte[]> report(@PathVariable ReportType type,
            @RequestParam(defaultValue = "CSV") ReportFormat format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ReportFile file = reportService.generate(type, format, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.content());
    }
}
