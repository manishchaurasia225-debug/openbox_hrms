# Architecture Decision Records (ADRs)

Lightweight log of notable technical decisions and deviations. Newest at top. Each: context →
decision → status/consequence.

## Documentation conflict-resolution precedence (approved 2026-07-19)

When documents conflict, resolve in this order and record the outcome here:

1. Latest approved user instruction
2. `project-rules.md`
3. `decisions.md` (this file)
4. `implementation-roadmap.md`
5. `requirements.md`
6. `permissions-matrix.md`
7. Any older documentation

Never stall on a conflict — resolve it with this order and, if material, add an ADR below.

---

## D-013 — Backend stabilization pass (security, config, API, audit) — 2026-07-21

**Context:** A verify-and-stabilize pass over the existing backend surfaced several defects while the
app was exercised end-to-end. **Decisions (all implemented, verified live, tests green):**

- **JWT env footgun.** `.env*` shipped `HRMS_JWT_SECRET=` (empty). The run scripts `source` env files
  with `set -a`, exporting the empty string, which *shadows* the dev-profile fallback
  (`${HRMS_JWT_SECRET:default}` only defaults when *unset*), so the app failed to start. Decided: never
  ship an empty assignment for a secret with a profile default — comment it out (unset) so the fallback
  applies; documented the trap in `.env.example` and README.
- **No default form-login user.** All auth is JWT (API chain) with the DB as the user store; the only
  server-rendered page (`/`) is public. Spring Boot's `UserDetailsServiceAutoConfiguration` was creating
  a throwaway `user` and logging its generated password every boot. Decided: exclude that auto-config
  (`spring.autoconfigure.exclude`) — no default credentials created or logged.
- **Configurable CORS.** There was no CORS policy. Added an env-driven allow-list
  (`hrms.security.cors.*`) on the API chain; **safe by default** (empty origins ⇒ no cross-origin
  access). Dev allows `localhost:5173/3000` for the SPA. Never combine `*` with credentials.
- **Client errors return 400, not 500.** A wrong-type path/query param
  (`MethodArgumentTypeMismatchException`) fell through to the generic 500 handler. Added explicit 400
  handlers (also `MissingServletRequestParameterException`) in `GlobalExceptionHandler`.
- **Audit columns store the email.** `created_by`/`updated_by` were persisting the principal record's
  `toString()` because `Authentication.getName()` fell through to it. Decided: `AuthenticatedUser`
  implements `AuthenticatedPrincipal` with `getName()` → email, so audit attribution is clean/stable.
- **Coverage visible.** Added JaCoCo (`test` finalizes with `jacocoTestReport`); baseline ~76% instr /
  ~75% line / 94% class over 101 tests.

**Status:** ✅ Resolved 2026-07-21. **Consequence:** These are additive/behaviour-preserving hardening
changes; the only intentional behaviour change is removing the accidental in-memory user.

## D-012 — Swagger (springdoc 3.0.x) + Actuator added for API docs & health

**Context:** The local-dev setup task required a working **Swagger** UI and a **health** endpoint, but
neither springdoc nor actuator was on the classpath, and the project runs on the bleeding-edge Spring
Boot **4.1.0** (Spring Framework 7), where the mainstream springdoc 2.x line (Boot 3 / Spring 6) does
not work. **Decision (user-requested, approved):** Add `spring-boot-starter-actuator` (ships with
Boot; exposes `/actuator/health,info`) and **`org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3`**
— the **3.0.x line is the Spring Boot 4.x-compatible** springdoc release (2.x would break). An
`OpenApiConfig` declares a global HTTP bearer (JWT) scheme; `SecurityConfig` permits
`/swagger-ui/**`, `/v3/api-docs/**`, and `/actuator/health` in the web chain (they are outside the
`/api/**` matcher). Swagger/api-docs are **disabled in the prod profile** by default
(`springdoc.*.enabled=${SPRINGDOC_ENABLED:false}`). **Status:** Accepted 2026-07-20; verified live —
full suite 98 tests green with both in the context, and `verify-dev.sh` confirms `/v3/api-docs` 200,
Swagger UI 200, and `/actuator/health` UP. **Consequence:** If springdoc lags a future Boot upgrade,
pin/upgrade the 3.x line together with Boot. These are the first steps of roadmap phases 14–16.

## D-011 — Audit capture via HTTP interceptor + REQUIRES_NEW producer

**Context:** Module 23 must audit login/logout, entity changes, config/permission changes, and
downloads without scattering audit calls across every service, and audit writes must never break or
roll back the business op they describe. **Decision:** (1) An `AuditInterceptor` (registered by a new
`WebMvcConfig`) records mutating requests (POST/PUT/PATCH/DELETE) and download/export GETs under
`/api/v1/**` generically — deriving action/module/entity from the HTTP method and path — so no service
code changes. (2) Auth events are recorded explicitly in `AuthServiceImpl.recordAttempt`/`logout`
(richer context: failure reason, actor on failed login). (3) The `AuditService` producer runs in
`Propagation.REQUIRES_NEW` and swallows failures, so a failed-login audit survives the request
rollback and a broken audit write never fails the operation. (4) The trail is append-only (no
update/delete API); the actor is denormalised (email + id, no FK). Audit queries use JPA
Specifications (not `:x is null or col = :x`, which sends untyped nulls PostgreSQL rejects). Gated by
`AUDIT:VIEW`. **Status:** Accepted 2026-07-20. **Consequence:** `@WebMvcTest` slices must mock
`AuditService` (the interceptor config needs it); non-transactional audit ITs must clean up committed
business entities (REQUIRES_NEW commits independently of the test transaction).

## D-010 — AI Assistant: tool-calling over services, offline provider, permission-filtered

**Context:** Module 21 requires an AI assistant, but CLAUDE.md mandates AI → tool-calling → services →
repos with permission checks, and no LLM credentials are configured. **Decision:** (1) Each capability
is an `AiTool` that wraps a **business service** (never a repository) and declares a `MODULE:ACTION`
authority. (2) The assistant filters tools to the caller's own authorities *before* planning and
re-checks at execution, so the AI can never reach data the user can't or escalate privilege — a
missing permission degrades gracefully to a capabilities message, not an error. (3) Intent routing is
a swappable `AiAssistantProvider`; the default `RuleBasedAiAssistantProvider` is deterministic and
offline (no creds), and a real LLM adapter (e.g. Anthropic Claude `claude-opus-4-8`) can be added
later as a `@Primary` bean without touching the tool layer or enforcement (same pattern as
D-002/D-009). **Approved by the user 2026-07-20** (chose "pluggable provider + offline default").
**Status:** Accepted. **Consequence:** Module 21 adds no persistence/table and no external dependency;
tools shipped: employee search, attendance search, report generation. Gated by `AI:VIEW` at the
endpoint plus per-tool module authority. Audit logging of AI interactions is deferred to Module 23.

## D-009 — WhatsApp delivery via a pluggable provider with a logging default

**Context:** Module 20 requires the Meta WhatsApp Business API, but no credentials are configured and
the platform is single-company/internal. Calling an external provider with no configuration is wrong,
yet the delivery/read tracking and message ledger must work end-to-end in dev/test. **Decision:**
Introduce a `WhatsAppProvider` interface (adapter) with a `LoggingWhatsAppProvider` default that
never calls out — it logs and returns an accepted result with a synthetic provider id. This mirrors
the email fallback (D-002). A production Meta adapter is added later as a `@Primary WhatsAppProvider`
bean, which wins injection without touching business code. (Note: `@ConditionalOnMissingBean` on the
scanned default proved unreliable and left no bean — use `@Primary` on the real one instead.)
**Status:** Accepted 2026-07-20. **Consequence:** No external dependency/credentials now; the
`whatsapp_messages` ledger records QUEUED→SENT→DELIVERED→READ/FAILED; status transitions are driven
by an authenticated endpoint standing in for the Meta webhook (webhook + signature verification is a
future addition). This is a null-object adapter, not a mock stub.

## D-008 — Automation Engine gated under `NOTIFICATION`, not a new permission module

**Context:** `requirements.md` names Module 18 "Automation Engine", but the canonical
`permissions-matrix.md` (D-005) defines **no** `AUTOMATION` module — automation is cross-cutting
infrastructure that orchestrates notification delivery. The matrix note for §12.2 states that a role's
`NOTIFICATION:ADMIN` "governs company notification configuration/templates", which is exactly what an
automation rule is. **Decision:** Do not add an `AUTOMATION` `PermissionModule`. Gate automation-rule
reads under `NOTIFICATION:VIEW` and rule administration + manual triggers under `NOTIFICATION:ADMIN`.
Automations dispatch through the existing channel-aware `NotificationService`; per-channel rendering
uses the Email (Module 19) and WhatsApp (Module 20) engines. **Status:** Accepted 2026-07-20.
**Consequence:** No new permission catalog rows / grant migration; Super Admin, Company Admin, and HR
Manager (the `NOTIFICATION:ADMIN` holders) manage automations. Scheduling is enabled via
`@EnableScheduling`; jobs are idempotent per (rule, date) through an `AutomationRun` ledger.

## D-007 — Single standardized `ApiResponse<T>` envelope for all responses

**Context:** `api-spec.md` documented a `{success, message, data, timestamp, requestId, errors}`
envelope, but the implemented `ApiErrorResponse` used a different shape and there was no success
wrapper. **Decision (approved):** Every API response — success **and** error — uses one
`ApiResponse<T>` envelope: `success`, `message`, `data`, `errors`, `path`, `timestamp`, `requestId`.
`ApiErrorResponse` is refactored/absorbed into this contract; `errors` carries per-field validation
details. **Status:** Accepted. **Consequence:** All controllers return `ApiResponse<T>`;
`GlobalExceptionHandler` emits the same envelope; OpenAPI documents it once.

## D-006 — `project-rules.md` precedence; removed features are not built

**Context:** `project-rules.md` lists "Features Explicitly Removed" that conflict with
`permissions-matrix.md`/`requirements.md` (e.g. Shift Management, document version history, the
Reporting-Manager/Work-Location/Confirmation-Date employee fields, PF/UAN/ESI/tax fields, GPS
attendance, Skills/Certificates/GitHub/Portfolio). **Decision (approved):** `project-rules.md` wins
per the precedence order; **do not implement removed features**, even where other docs mention them.
**Status:** Accepted. **Consequence:** SHIFT and DOC-versioning rows in the permissions matrix are
treated as out-of-scope; attendance is Wi-Fi/IP based (no GPS); employee master omits the removed
fields.

## D-005 — `permissions-matrix.md` is the canonical role & permission model (9 roles)

**Context:** Three role models existed (5-role in `requirements.md`, 9-role in
`permissions-matrix.md`, 10-role in the vision doc). **Decision (approved):** The **9-role** model in
`permissions-matrix.md` is canonical: Super Admin, Company Admin, HR Manager, HR Executive, Manager,
Team Lead, Employee, Recruiter, Finance (Candidate is external/portal-only, outside the internal
grid). **Status:** Accepted. **Consequence:** RBAC seed data, role enums, and every authorization
test derive from the matrix; the `requirements.md` 5-role list is superseded.

## D-004 — Single-company now; multi-tenancy deferred but not precluded

**Context:** `project-rules.md`/`requirements.md` say "NOT a SaaS app," while the vision/permissions
docs assume multi-tenancy. **Decision (approved):** Build **single-company**. **Do not** add
`tenant_id` columns or tenant-aware logic now. **Do** architect so multi-tenancy can be added later
without major rewrites: isolate cross-cutting access behind repositories/services, keep a single
company-scoped configuration surface, and avoid assumptions that hardcode "one and only one" beyond
what's necessary. **Status:** Accepted. **Consequence:** "all-tenants"/Company-Management/tenant-
isolation parts of the permissions matrix and `security.md` are inert for now; Super Admin operates
within the single company.

---

## D-003 — Stateless JWT authentication (supersedes interim form login)

**Context:** The foundation shipped with Spring Security form login as a placeholder. **Decision
(approved):** Implement stateless **JWT** (access + refresh) on a dedicated `/api/**` security chain
(CSRF disabled there), with a shared `BCryptPasswordEncoder` and a persistent user/role/permission
store. **Status:** Accepted — implementing in Phase 1 Module 2. **Consequence:** The form-login chain
is replaced/retained only for any server-rendered pages; API auth is token-based.

## D-002 — Secrets come from the environment (resolved)

**Context:** A real DB password had been placed as a hardcoded YAML default in the working tree.
**Decision:** Secrets resolve from env vars via git-ignored `.env.dev`/`.env.prod`; YAML defaults are
empty (dev) or absent (prod). **Status:** ✅ Resolved 2026-07-19 — dev is `${DB_PASSWORD:}`, prod is
`${DB_PASSWORD}` (fails fast). Never committed. **Consequence:** `.env*` git-ignored except the
example; `run-prod.sh` asserts required secrets.

## D-001 — Stay on Spring Boot 4.x (resolved)

**Context:** The original brief said Spring Boot 3.x; the project was generated on Spring Boot
**4.1.0** (Java 21, Gradle 9.5.1, Jackson 3 `tools.jackson`, `spring-boot-starter-webmvc`, and the
relocated `@WebMvcTest` in `org.springframework.boot.webmvc.test.autoconfigure`). **Decision
(approved):** **Stay on 4.x.** The "Spring Boot 3" references in `CLAUDE.md`/`tech-stack.md` are
outdated documentation and are updated to 4.x. **Do not downgrade.** **Status:** ✅ Resolved
2026-07-19. **Consequence:** Validate every dependency against 4.x/Jackson 3, not Boot-3 examples;
treat the installed version as source of truth.

---

> Add an ADR whenever a decision is hard to reverse, deviates from a doc, or future engineers would
> otherwise re-litigate. Keep them short.
