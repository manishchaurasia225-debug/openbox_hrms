package com.ogm.hrms.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default {@link VirusScanner} that performs no scanning (placeholder for a real integration).
 * Kept as a distinct bean so a production scanner can override it via {@code @Primary} or profiles.
 */
@Component
public class NoOpVirusScanner implements VirusScanner {

    private static final Logger log = LoggerFactory.getLogger(NoOpVirusScanner.class);

    @Override
    public void scan(String filename, byte[] content) {
        log.debug("Virus scan skipped (no scanner configured) for '{}' ({} bytes)", filename,
                content != null ? content.length : 0);
    }
}
