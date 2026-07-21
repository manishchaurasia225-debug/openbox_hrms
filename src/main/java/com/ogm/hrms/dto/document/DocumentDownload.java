package com.ogm.hrms.dto.document;

import org.springframework.core.io.Resource;

/** A resolved document ready to stream to the client (binary — not wrapped in ApiResponse). */
public record DocumentDownload(
        Resource resource,
        String filename,
        String contentType
) {
}
