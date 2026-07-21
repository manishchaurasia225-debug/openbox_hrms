---

# Database Design Responsibilities

Before creating any entity, table, migration, or repository:

1. Analyze the module requirements.
2. Identify all required entities.
3. Identify relationships.
4. Identify lookup/master tables.
5. Identify configurable tables.
6. Identify audit requirements.
7. Identify reporting requirements.
8. Identify search requirements.
9. Identify future scalability concerns.
10. Suggest improvements before implementation.

Never directly create tables without first validating the data model.

---

# Module Design Guidelines

For every module, Claude must produce:

- Business Entities
- Database Tables
- Relationships
- Primary Keys
- Foreign Keys
- Constraints
- Indexes
- Composite Indexes (where beneficial)
- Unique Constraints
- Nullable vs Non-nullable fields
- Enum Strategy
- Cascade Rules
- Soft Delete Strategy
- Audit Strategy

Always explain major design decisions before implementation.

---

# Naming Standards

Use consistent naming throughout the project.

Tables

- snake_case
- Singular or plural consistently (prefer singular if not already decided)

Columns

- snake_case

Foreign Keys

- <entity>_id

Primary Key

- id

Boolean Columns

- is_active
- is_deleted
- is_verified

Date Columns

- created_at
- updated_at
- deleted_at

---

# Performance Guidelines

Every table should be reviewed for:

- Query frequency
- Expected row count
- Growth over time
- Index requirements
- Reporting impact
- Archive strategy

Avoid N+1 query problems.

Recommend batching where appropriate.

---

# Security Guidelines

Sensitive data should be encrypted where appropriate.

Never expose confidential fields unnecessarily.

PII should only be accessible to authorized roles.

Always consider GDPR/privacy-friendly design principles.

---

# Future Expansion

The schema should support future modules without major redesign.

Future modules may include:

- Payroll
- Recruitment
- Performance Management
- Asset Management
- IT Helpdesk
- Visitor Management
- Learning Management
- Finance
- Procurement

Design current entities with future relationships in mind.

---

# Claude Enhancement

Always use your full Claude Code expertise to improve the database architecture before implementation.

When designing any schema:

- Challenge the current design.
- Look for normalization improvements.
- Eliminate redundant data.
- Recommend better relationships.
- Suggest indexing strategies.
- Identify edge cases.
- Consider high-volume enterprise deployments.
- Ensure long-term maintainability.

If a better database design exists, recommend it before generating code.

---

# As-Built State (current codebase — 2026-07-19)

> The design responsibilities above are the process to follow. This is the current DB reality.

- **Engine/config:** PostgreSQL via Spring Data JPA/Hibernate. `open-in-view: false` (explicit
  transaction boundaries). Local default `jdbc:postgresql://localhost:5432/hrms`, overridable by
  `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`.
- **Schema strategy:** dev `ddl-auto: update` (+ `show-sql`); prod `ddl-auto: validate` (Hibernate
  never mutates prod schema). **No migration tool (Flyway/Liquibase) is wired yet** — prod schema
  changes will need one before go-live.
- **No entities, tables, or repositories exist yet.** The naming/audit/soft-delete/index conventions
  above are not yet materialized in code — apply them as each module's data model is designed.
- **Connection pool:** HikariCP in prod (`DB_POOL_MAX`=10, `DB_POOL_MIN`=2 defaults).
- **Secret hygiene:** a real DB password is currently hardcoded as a YAML default in the working tree
  (`${DB_PASSWORD:Awanish@84OGM}`) — uncommitted, but should be reverted to `${DB_PASSWORD:}`. See
  `decisions.md` D-002.

## Claude Code Enhancement

Always use your maximum reasoning capabilities before responding.

Think like:

- Principal Software Engineer
- Enterprise Architect
- Database Architect
- Security Engineer
- DevOps Engineer
- QA Engineer
- Performance Engineer
- Product Engineer

Before implementation:

- Challenge existing assumptions.
- Look for simpler and better solutions.
- Identify missing requirements.
- Identify edge cases.
- Identify security concerns.
- Identify scalability concerns.
- Suggest enterprise-grade improvements.
- Preserve backward compatibility.
- Never reduce functionality unless explicitly instructed.

Your objective is not merely to produce working code, but to deliver the highest-quality, production-ready solution aligned with the project's architecture, standards, and long-term maintainability.
