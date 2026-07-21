package com.ogm.hrms.report;

/** A rendered report ready to stream to the client. */
public record ReportFile(byte[] content, String filename, String contentType) {
}
