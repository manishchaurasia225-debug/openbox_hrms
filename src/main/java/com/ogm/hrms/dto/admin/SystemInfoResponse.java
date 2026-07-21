package com.ogm.hrms.dto.admin;

import java.time.OffsetDateTime;
import java.util.List;

/** System health & information summary for the administration console. */
public record SystemInfoResponse(
        String applicationName,
        String version,
        List<String> activeProfiles,
        OffsetDateTime startedAt,
        long uptimeSeconds,
        String databaseStatus,
        Counts counts
) {
    /** Live record counts across core entities. */
    public record Counts(long users, long employees, long departments, long roles, long permissions) {
    }
}
