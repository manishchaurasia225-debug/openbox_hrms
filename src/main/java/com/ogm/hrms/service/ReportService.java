package com.ogm.hrms.service;

import com.ogm.hrms.enums.ReportFormat;
import com.ogm.hrms.enums.ReportType;
import com.ogm.hrms.report.ReportFile;

import java.time.LocalDate;

/** Report generation & export (RBAC module {@code REPORT}). */
public interface ReportService {

    ReportFile generate(ReportType type, ReportFormat format, LocalDate from, LocalDate to);
}
