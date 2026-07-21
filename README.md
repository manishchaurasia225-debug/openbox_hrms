# OGM HRMS — Enterprise HR Operations Platform

A production-grade, single-company internal **HR Operations Platform**: the single source of truth for
employee information and HR operations. Backend is feature-complete across 24 modules (auth/RBAC,
employees, attendance, leave, documents, payroll, reimbursements, dashboards, reports, automation,
email/WhatsApp, AI assistant, global search, audit, and system administration).

- **Stack:** Java 21 · Spring Boot 4.1 · Spring Security (JWT) · Spring Data JPA/Hibernate ·
  PostgreSQL · Flyway · Gradle (Kotlin DSL) · springdoc-openapi (Swagger) · Actuator
- **Testing:** JUnit 5 · Testcontainers (real PostgreSQL for integration tests)

---

## TL;DR — start in three commands

```bash
cp .env.example .env      # then edit DB_* and SUPER_ADMIN_*
# (make sure PostgreSQL is running and the 'hrms' database exists — see below)
./scripts/run-dev.sh
```

Then log in with the `SUPER_ADMIN_EMAIL` / `SUPER_ADMIN_PASSWORD` you set in `.env`, and open the API
docs at <http://localhost:8080/swagger-ui.html>.

---

## 1. Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| **JDK** | 21 | The Gradle toolchain resolves it; `java -version` should show 21 if running outside Gradle. |
| **Docker** | any recent | Required to run PostgreSQL locally **and** for the integration test suite (Testcontainers). |
| **PostgreSQL** | 16/17 | Run via Docker (below) or a local install. |
| **Git** | any | To clone the repo. |
| `psql` (optional) | — | Enables the DB checks in `scripts/verify-dev.sh`. Ships with the Postgres client. |

You do **not** need to install Gradle — the repo includes the Gradle wrapper (`./gradlew`).

---

## 2. PostgreSQL setup

### Option A — Docker (recommended)

```bash
docker run --name hrms-postgres \
  -e POSTGRES_DB=hrms \
  -e POSTGRES_USER=hrms \
  -e POSTGRES_PASSWORD=hrms \
  -p 5432:5432 -d postgres:17
```

That creates the `hrms` database automatically. Set `DB_USERNAME=hrms` / `DB_PASSWORD=hrms` in your
`.env` to match.

### Option B — existing/local PostgreSQL

Create the database once:

```bash
createdb -h localhost -p 5432 hrms
# or:  psql -h localhost -U postgres -c 'CREATE DATABASE hrms;'
```

The application does **not** create the database itself — but it **does** create all tables via Flyway
migrations on startup (you don't run any DDL by hand).

---

## 3. Environment setup

All configuration comes from environment variables, loaded from a git-ignored `.env` (or `.env.dev`).
Copy the template and edit it:

```bash
cp .env.example .env
```

At minimum set:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — your PostgreSQL connection.
- `SUPER_ADMIN_EMAIL`, `SUPER_ADMIN_PASSWORD` — the first admin to bootstrap (see below).

Everything else has a sensible default. `.env.example` documents every variable. The dev profile ships
an **insecure** JWT secret so the app boots without `HRMS_JWT_SECRET`; always set a real one outside
local dev (`openssl rand -base64 48`).

> **Do not assign `HRMS_JWT_SECRET=` an empty value.** An empty assignment is exported by the run
> scripts and *overrides* the dev fallback with a blank secret, so the app refuses to start. Leave the
> line commented out (unset) in dev, or set a real secret. Same applies to any other secret with a
> profile default.

For a browser SPA on another origin, set `HRMS_CORS_ALLOWED_ORIGINS` (comma-separated). The dev profile
already allows `http://localhost:5173` and `http://localhost:3000`; with none configured, no
cross-origin access is granted (the safe default).

> Secrets live only in `.env*` (git-ignored except `.env.example`) — never commit real credentials.

---

## 4. First startup & the Super Admin bootstrap

The application seeds **no user accounts** by default — only roles and the permission catalogue. On the
**first** startup, if **both** `SUPER_ADMIN_EMAIL` and `SUPER_ADMIN_PASSWORD` are set **and** no user
with that email exists, a single **Super Admin** is created so you can log in.

- **Login is by email** — there is no separate username.
- The step is **idempotent**: once the admin exists, these values are ignored on later startups.
- The password is stored **BCrypt-hashed**, never in plaintext.
- If the values are unset, startup logs *"No hrms.bootstrap.super-admin credentials configured;
  skipping admin bootstrap"* and you won't be able to log in until a user exists.

Start the app:

```bash
./scripts/run-dev.sh
```

`run-dev.sh` loads `.env`/`.env.dev`, verifies required variables, checks that PostgreSQL is reachable,
prints the Swagger/health URLs, then runs the app. On success you'll see Flyway apply the migrations and
a log line like `Bootstrapped Super Admin user 'admin@example.com'` (first run only).

### Log in

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"<your SUPER_ADMIN_PASSWORD>"}'
```

The response contains `data.accessToken` — send it as `Authorization: Bearer <token>` on subsequent
API calls (or paste it into Swagger UI's **Authorize** dialog).

---

## 5. API docs, health & endpoints

| What | URL |
|------|-----|
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Health | <http://localhost:8080/actuator/health> |
| API base | `http://localhost:8080/api/v1/...` |

Swagger UI, the OpenAPI document, and the health endpoint are public; all `/api/v1/**` endpoints (except
the auth token endpoints) require a JWT. Every operation carries a summary/description and is grouped by
tag in Swagger UI.

### Postman

A ready-to-import Postman collection covering **every** endpoint (generated from the live OpenAPI
document, so it stays in sync with the API) lives in [`postman/`](postman/):

- `postman/hrms.postman_collection.json` — 32 folders, all API operations, collection-level Bearer auth.
- `postman/hrms-local.postman_environment.json` — the *HRMS – Local (dev)* environment.

Import both, select the environment, then run **Authentication › Login** once — its test script captures
the token into the `{{accessToken}}` variable, so every other request is authorized automatically. Set
the `adminEmail` / `adminPassword` variables to your bootstrapped Super Admin first.

---

## 6. Verify the environment

With the app running, in another terminal:

```bash
./scripts/verify-dev.sh
```

It reports **PASS/FAIL** for: environment variables loaded, database connectivity, Flyway migration
status, seeded roles & permissions, Super Admin existence, health endpoint, login endpoint, an
authenticated request, and the OpenAPI/Swagger endpoints. Exit code is non-zero if any check fails.

---

## 7. Running tests

```bash
./gradlew build          # compile + run the full unit + integration suite
./gradlew test           # tests only
```

Integration tests use **Testcontainers**, which starts a throwaway PostgreSQL container — so **Docker
must be running**. No local database or `.env` is needed for tests (they are fully self-contained).

Coverage is reported by **JaCoCo**, generated automatically after the test run:

```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html    # human-readable HTML report
```

The suite (98 tests across unit + integration) currently covers ~76% of instructions / ~75% of lines.

---

## 8. Profiles & production

| Profile | Purpose |
|---------|---------|
| `dev` (default) | `ddl-auto=validate`, SQL logging, insecure fallback JWT secret for convenience. |
| `prod` | `ddl-auto=validate`, connection pool tuning, no SQL logging, no stack traces to clients. Requires **all** secrets via the environment (`run-prod.sh` fails fast if missing). |

Activate prod with `SPRING_PROFILES_ACTIVE=prod` (or `--spring.profiles.active=prod`) and run
`./scripts/run-prod.sh` (builds a bootJar and launches it). In production, supply `HRMS_JWT_SECRET`,
`DB_*`, and the `SUPER_ADMIN_*` bootstrap via real environment variables or a secrets manager — never
files. See [`.claude/deployment.md`](.claude/deployment.md) for details.

> Redis caching and AWS S3 document storage are on the roadmap but **not yet wired** into the running
> app; document binaries currently persist to the local filesystem (`HRMS_STORAGE_PATH`), and email is
> logged rather than sent unless you configure SMTP.

---

## 9. Troubleshooting

| Symptom | Likely cause / fix |
|---------|--------------------|
| `run-dev.sh` says *PostgreSQL is NOT reachable* | Start Postgres (section 2) and confirm host/port in `DB_URL`. |
| App fails at startup with a Flyway/`schema validation` error | The DB schema is out of sync. For a fresh dev DB, drop and recreate it so Flyway re-applies from V1: `dropdb hrms && createdb hrms`. |
| `FlywayValidateException` / checksum mismatch | A migration file changed after being applied. Use a clean database in dev. |
| Login returns 401 | Wrong email/password, or no Super Admin was bootstrapped — check the startup log and that `SUPER_ADMIN_*` were set **before first startup**. |
| Can't log in on a DB that already has data | The bootstrap only runs when the user doesn't exist. Set `SUPER_ADMIN_*` to an email that isn't present, or create a user via the admin API. |
| `HRMS_JWT_SECRET` error outside dev | The secret must be ≥ 32 bytes. Generate: `openssl rand -base64 48`. |
| App won't start in dev: *HRMS_JWT_SECRET is not configured* | Your `.env` assigns `HRMS_JWT_SECRET=` (empty), which overrides the dev fallback with a blank secret. Comment the line out (leave it unset) or set a real secret. |
| Browser SPA gets a CORS error calling the API | Add the SPA's origin to `HRMS_CORS_ALLOWED_ORIGINS` (comma-separated). Dev already allows `localhost:5173`/`3000`. |
| Tests fail to start containers | Docker isn't running, or the daemon isn't reachable by Testcontainers. |
| Port 8080 in use | Set `SERVER_PORT` in `.env`. |

---

## Project layout (high level)

```
src/main/java/com/ogm/hrms/
  config/       Spring config (security, JPA auditing, OpenAPI, scheduling, bootstrap seeding)
  security/     JWT, RBAC matrix, authenticated principal
  entity/       JPA entities (extend BaseEntity: audit columns, soft-delete, versioning)
  repository/   Spring Data repositories
  service/      Business services (interfaces) + service/impl (implementations)
  controller/   REST controllers (/api/v1/**)
  dto/          Request/response records per feature
  audit/ ai/ search/ automation/ report/ whatsapp/ storage/   feature-specific packages
src/main/resources/db/migration/   Flyway migrations (V1..Vnn) — own the schema
scripts/        run-dev.sh, run-prod.sh, verify-dev.sh
```

Detailed engineering docs live under [`.claude/`](.claude/) (architecture, decisions/ADRs, deployment,
testing, module status).
