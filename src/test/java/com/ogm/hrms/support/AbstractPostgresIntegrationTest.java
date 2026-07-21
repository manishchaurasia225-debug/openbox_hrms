package com.ogm.hrms.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests that require a real database. Boots the full Spring context
 * against a Testcontainers-managed <strong>PostgreSQL</strong> instance (never H2), so Flyway
 * migrations, entity-to-schema validation, PostgreSQL-specific SQL, transactions, and repository
 * behavior are all exercised against the production database engine.
 *
 * <p>The container is started once per JVM (singleton pattern) and reused across every integration
 * test class for speed; Testcontainers stops it via the Ryuk resource reaper on shutdown. Flyway is
 * enabled and Hibernate runs in {@code validate} mode, so a mismatch between a migration and an
 * entity fails the build.</p>
 */
@SpringBootTest
public abstract class AbstractPostgresIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("hrms")
                    .withUsername("hrms")
                    .withPassword("hrms");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.show-sql", () -> "false");
        // Deterministic JWT secret for tests (>= 32 bytes); never a production value.
        registry.add("hrms.security.jwt.secret",
                () -> "test-only-jwt-secret-please-change-in-every-real-environment-0123456789");
        // Document storage under the OS temp dir so tests never write into the project tree.
        registry.add("hrms.storage.base-path",
                () -> System.getProperty("java.io.tmpdir") + "/hrms-test-documents");
    }
}
