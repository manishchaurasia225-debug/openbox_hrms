package com.ogm.hrms.report;

import java.util.List;

/** Tabular report content: a title, column headers, and string rows. Format-agnostic. */
public record ReportData(String title, List<String> headers, List<List<String>> rows) {
}
