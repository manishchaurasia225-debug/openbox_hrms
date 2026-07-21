package com.ogm.hrms.storage;

/**
 * Hook for scanning uploaded content before it is stored. The default implementation is a no-op; a
 * real scanner (e.g. ClamAV) can replace it without changing the upload flow. Implementations should
 * throw if content is rejected.
 */
public interface VirusScanner {

    void scan(String filename, byte[] content);
}
