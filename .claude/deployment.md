# Deployment

## Profiles

| Profile | File | Purpose |
|---------|------|---------|
| (base) | `application.yaml` | Shared config; `spring.profiles.default: dev`; `open-in-view: false` |
| dev | `application-dev.yaml` | `ddl-auto: update`, `show-sql: true`, debug logging (Hibernate SQL, Spring Security) |
| prod | `application-prod.yaml` | `ddl-auto: validate`, Hikari pool, no SQL logging, no stack traces to clients, Thymeleaf cache on |

Activate with `SPRING_PROFILES_ACTIVE=prod` (or `--spring.profiles.active=prod`). Default is **dev**.

## Configuration & secrets

All environment-specific values come from environment variables, resolved from git-ignored
`.env.dev` / `.env.prod` (template: [`.env.example`](../.env.example)):

`SPRING_PROFILES_ACTIVE` · `DB_URL` · `DB_USERNAME` · `DB_PASSWORD` · `SERVER_PORT` ·
`JPA_DDL_AUTO` · `DB_POOL_MAX` · `DB_POOL_MIN` ·
`SUPER_ADMIN_EMAIL` · `SUPER_ADMIN_PASSWORD` · `SUPER_ADMIN_FULL_NAME` (first-run bootstrap, below).

**Never commit real secrets** into `application*.yaml` (see [`decisions.md`](./decisions.md) D-002).
`.env*` is git-ignored except the example.

## Run scripts

```bash
./scripts/run-dev.sh    # loads .env.dev, SPRING_PROFILES_ACTIVE=dev, ./gradlew bootRun (+ devtools)
./scripts/run-prod.sh   # loads .env.prod, asserts DB_URL/DB_USERNAME/DB_PASSWORD present,
                        # ./gradlew clean bootJar, then java -jar build/libs/hrms-*.jar
```

`run-prod.sh` **fails fast** if required secrets are missing.

## First-run bootstrap (initial Super Admin)

The application **seeds no user accounts by default** — only roles and the permission catalogue.
Authentication is by **email** (there is no separate username field). To get a first account you
bootstrap a single **Super Admin** via configuration on first startup:

| Variable | Purpose | Default |
|----------|---------|---------|
| `SUPER_ADMIN_EMAIL` | Login email of the bootstrapped admin | *(empty → no admin created)* |
| `SUPER_ADMIN_PASSWORD` | Its password (stored **BCrypt-hashed**, never plaintext) | *(empty → no admin created)* |
| `SUPER_ADMIN_FULL_NAME` | Display name | `System Administrator` |

**How it works** (`config/DataInitializer.bootstrapSuperAdminUser`):

1. On every startup, if **both** email and password are set **and** no user with that email exists,
   one Super Admin (role `SUPER_ADMIN`, `enabled`+`emailVerified`) is created.
2. If either value is blank, bootstrap is skipped and the log reads
   *"No hrms.bootstrap.super-admin credentials configured; skipping admin bootstrap."* — you cannot
   log in until an admin exists.
3. The step is **idempotent**: once that Super Admin exists, the bootstrap credentials are
   **ignored on all subsequent startups** (they do not update or re-create the account).

**First run (local dev):**

```bash
cp .env.example .env.dev
# edit .env.dev: set a real DB_PASSWORD, and set the bootstrap admin, e.g.
#   SUPER_ADMIN_EMAIL=you@yourcompany.com
#   SUPER_ADMIN_PASSWORD=<a-strong-password-you-choose>
./scripts/run-dev.sh
# then authenticate: POST /api/v1/auth/login  { "email": "...", "password": "..." }
```

**Security rules:**

- The `.env.example` placeholders (`admin@example.com` / `CHANGE_ME_TO_A_STRONG_PASSWORD`) are for
  local development **only** — never use them as-is.
- These variables **must never contain production credentials** and must not be committed with real
  values (`.env*` is git-ignored except the example).
- **Production must override them via real environment variables or a secrets manager** (never baked
  into images or config files). Rotate/retire the bootstrap password after the account is created and
  its own password is changed.

## Build

```bash
./gradlew clean bootJar        # produces build/libs/hrms-<version>.jar (the -plain jar is not runnable)
./gradlew compileJava compileTestJava   # compile-only check
```

Java 21 toolchain (Gradle resolves/downloads it); wrapper is Gradle 9.5.1.

## Target deployment (from the brief — not yet implemented)

- **Docker** + **Docker Compose** (app + Postgres + Redis) — no `Dockerfile`/compose file yet.
- **GitHub Actions** CI/CD (build, test, image) — no workflow yet.
- **AWS EC2** runtime, **AWS S3** for documents, **Nginx** reverse proxy/TLS.
- Externalized config & secrets via the environment (never baked into images).

## Ops expectations

Structured logging (no secrets/passwords), health/readiness endpoints, DB migrations via
Flyway/Liquibase (not `ddl-auto`) in prod, and maintenance scheduled around payroll cut-offs
(availability on critical paths).
