package com.ogm.hrms;

import com.ogm.hrms.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Full-context integration test. Boots the application against a Testcontainers PostgreSQL instance,
 * which verifies that Flyway migrations apply cleanly and that Hibernate ({@code ddl-auto=validate})
 * confirms every entity matches the migrated schema.
 */
class HrmsApplicationTests extends AbstractPostgresIntegrationTest {

    @Test
    void contextLoads() {
    }
}
