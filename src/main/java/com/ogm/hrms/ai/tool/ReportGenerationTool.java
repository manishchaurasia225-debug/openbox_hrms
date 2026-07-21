package com.ogm.hrms.ai.tool;

import com.ogm.hrms.ai.AiTool;
import com.ogm.hrms.ai.AiToolRequest;
import com.ogm.hrms.ai.AiToolResult;
import com.ogm.hrms.enums.ReportFormat;
import com.ogm.hrms.enums.ReportType;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.report.ReportFile;
import com.ogm.hrms.service.ReportService;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** AI tool: generates a report. Wraps {@link ReportService}; requires REPORT:EXPORT. */
@Component
public class ReportGenerationTool implements AiTool {

    private final ReportService reportService;

    public ReportGenerationTool(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public String name() {
        return "report_generation";
    }

    @Override
    public String description() {
        return "Generate a report. Parameters: type (EMPLOYEE|DEPARTMENT|LEAVE|ATTENDANCE|SALARY, "
                + "default EMPLOYEE), format (CSV|EXCEL|PDF, default CSV).";
    }

    @Override
    public String requiredAuthority() {
        return "REPORT:EXPORT";
    }

    @Override
    public AiToolResult execute(AiToolRequest request) {
        ReportType type = parseType(request.param("type", ReportType.EMPLOYEE.name()));
        ReportFormat format = parseFormat(request.param("format", ReportFormat.CSV.name()));
        ReportFile file = reportService.generate(type, format, null, null);
        String download = "/api/v1/reports/" + type.name() + "?format=" + format.name();
        String summary = "Generated the " + type.name() + " report as " + format.name() + " ("
                + file.content().length + " bytes). Download it from " + download + ".";
        return new AiToolResult(summary,
                new ReportInfo(type, format, file.filename(), file.contentType(), file.content().length, download));
    }

    private ReportType parseType(String raw) {
        try {
            return ReportType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Unknown report type '" + raw + "'");
        }
    }

    private ReportFormat parseFormat(String raw) {
        try {
            return ReportFormat.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Unknown report format '" + raw + "'");
        }
    }

    public record ReportInfo(ReportType type, ReportFormat format, String filename, String contentType,
                             int sizeBytes, String download) {
    }
}
