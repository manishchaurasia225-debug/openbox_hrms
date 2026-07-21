package com.ogm.hrms.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Document storage configuration. The active backend is local filesystem; an S3 backend can be
 * introduced behind {@link StorageService} without changing callers.
 */
@ConfigurationProperties(prefix = "hrms.storage")
public record StorageProperties(
        @DefaultValue("storage/documents") String basePath,
        @DefaultValue("10485760") long maxFileSizeBytes,
        @DefaultValue({
                "application/pdf",
                "image/png",
                "image/jpeg",
                "image/jpg",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain"
        }) List<String> allowedContentTypes
) {
}
