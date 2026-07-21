package com.ogm.hrms.storage;

import org.springframework.core.io.Resource;

/**
 * Abstraction over the document storage backend. Callers depend on this interface, not on the
 * filesystem or a cloud provider, so the backend can change (local → S3) without touching services.
 */
public interface StorageService {

    /**
     * Stores content under the given logical folder and returns an opaque, unique storage key.
     */
    String store(String folder, String originalFilename, byte[] content);

    /** Loads previously stored content as a readable resource. */
    Resource load(String storageKey);

    /** Removes stored content; missing content is not an error. */
    void delete(String storageKey);
}
