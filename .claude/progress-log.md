# Progress Log

> Resume point for autonomous implementation. Newest entry at top. Each entry: what shipped, how it
> was verified, and the exact next step. Branch: `develop` (all work pushed to GitHub).

## 🎉 ALL 24 MODULES / 10 PHASES COMPLETE — backend feature-complete (2026-07-20)
- Remaining non-module work (per implementation-roadmap phases 14–16, out of the 24-module scope):
  ~~OpenAPI/Swagger~~ (done), React frontend, Redis caching, Docker packaging, CI/CD. Pick up here.

## 2026-07-20 — Local dev environment + Swagger/health (verified live)  ✅ pushed
Made a fresh clone runnable with only `cp .env.example .env` + edit + `./scripts/run-dev.sh`.
- Added **Swagger** (`springdoc-openapi-starter-webmvc-ui:3.0.3` — the Boot-4.x line; 2.x is Boot-3
  only) + **Actuator** health/info (D-012). `OpenApiConfig` (JWT bearer scheme); `SecurityConfig`
  permits swagger/api-docs/actuator-health; prod disables swagger by default.
- `.env.example` rewritten: every real var documented in sections (DB/JWT/app/storage/bootstrap/
  optional SMTP/reserved Redis+S3). **Bug fixed:** unquoted `SUPER_ADMIN_FULL_NAME=System Administrator`
  aborted `set -a; source` under `set -e` — quoted it and hardened the loader to warn-not-abort.
- `run-dev.sh`: loads `.env` then `.env.dev`, verifies required vars + Postgres reachability, prints
  URLs. New `verify-dev.sh`: 12 PASS/FAIL checks (env, DB, Flyway, roles/perms, admin, health, login,
  authed request, api-docs, swagger-ui). New comprehensive `README.md`.
- Bootstrap now logs all 3 paths: created / **already-exists (idempotent)** / missing-config.
- **Verified live** against Dockerized Postgres 17: app boots, Flyway V1–V22, Super Admin bootstrapped
  on first run + idempotent on restart, login returns token, /auth/me 200, Swagger + health up —
  `verify-dev.sh` 12/12 PASS twice. Full suite still 98/98 green.

## 2026-07-20 — Module 24: System Administration  ✅ pushed — MODULE 24 & PHASE 10 & PROJECT COMPLETE
Admin console API `/api/v1/admin`: `system-info` (app name/version, active profiles, uptime, DB
status, live counts of users/employees/departments/roles/permissions), `roles` (role→permission
catalogue projected from the canonical `RolePermissionMatrix`), `login-history` (paged, optional
email filter; added all-history repo finder). Composes existing repos/matrix — settings CRUD and user
admin (their own modules) are NOT duplicated. Gated SETTINGS:ADMIN / AUTHZ:VIEW / AUDIT:VIEW. No new
table. Verified vs real Postgres: 98 tests (AdminIT ×4: system-info, roles catalogue [9 roles],
login-history, RBAC deny). Final full build green: 98 tests / 36 classes / 22 Flyway migrations /
bootJar built.

## 2026-07-20 — Module 23: Audit Logs  ✅ pushed — MODULE 23 COMPLETE (Phase 10 50%)
Append-only `audit_logs` (Flyway V22) + `AuditService` producer (REQUIRES_NEW, best-effort so audits
survive business rollback and never break the op). `AuditInterceptor` (WebMvcConfig) captures
mutating requests + downloads generically — action/module/entity derived from method+path — with
explicit login/logout/failed-login capture in `AuthServiceImpl`. Filtered query API (JPA
Specifications, avoiding untyped-null params) under AUDIT:VIEW. Decision D-011. Verified vs real
Postgres: 94 tests (AuditIT ×4: login recorded, failed-login recorded despite 401, mutating POST
captured, RBAC deny). Fixed 2 regressions: @WebMvcTest slice needed a mocked AuditService; non-tx
AuditIT cleans up its committed department to not pollute OrgMasterIT.

## 2026-07-20 — Module 22: Global Search  ✅ pushed — MODULE 22 & PHASE 9 COMPLETE
Unified `/api/v1/search?q=&limit=`: pluggable `EntitySearcher` per type (Employee via
EmployeeService.search, Department/Designation via new repo search finders); `GlobalSearchServiceImpl`
runs only searchers whose required module VIEW authority the caller holds → typed grouped results, no
cross-module leakage. Authenticated endpoint. Verified vs real Postgres: 90 tests (GlobalSearchIT ×2
multi-type + no-match; GlobalSearchServiceImplTest ×2 unit — permission filter skips unauthorized
searcher + blank-query guard, since all seeded roles happen to hold VIEW on the master-data types).

## 2026-07-20 — Module 21: Enterprise AI Assistant  ✅ pushed `a3e57c4` — MODULE 21 COMPLETE (Phase 9 50%)
Tool-calling over services (3 tools), swappable offline provider (D-010), permission-filtered. 86 tests.

## 2026-07-20 — Module 21: Enterprise AI Assistant  ✅ pushed — MODULE 21 COMPLETE (Phase 9 50%)
Tool-calling layer per CLAUDE.md AI Rules (AI→tools→services→repos). `AiTool` interface + 3 tools
wrapping services (EmployeeSearchTool→EmployeeService.search [new], AttendanceSearchTool→
AttendanceService.list, ReportGenerationTool→ReportService.generate). Swappable `AiAssistantProvider`
with deterministic offline `RuleBasedAiAssistantProvider` default (D-010; user-approved). Permission
enforcement: tools filtered to caller authorities before planning + re-checked at execution — no
escalation. `/api/v1/ai/assistant` + `/tools` under AI:VIEW. Stateless (no table). Verified vs real
Postgres: 86 tests (AiAssistantIT ×4: employee/attendance/report routing + TEAM_LEAD cannot invoke
report tool it lacks REPORT:EXPORT for).

## 2026-07-20 — Module 20: WhatsApp Integration  ✅ pushed — MODULE 20 & PHASE 8 COMPLETE
`WhatsAppProvider` adapter + `LoggingWhatsAppProvider` no-op default (D-009); `WhatsAppTemplate`
(Meta category taxonomy) + `WhatsAppMessage` ledger, Flyway V21. Template CRUD + preview; send →
provider → ledger (SENT/FAILED + providerMessageId); delivery/read lifecycle via
`PATCH /messages/{id}/status` (Meta webhook stand-in). Two controllers under WHATSAPP:VIEW/CREATE/
EDIT/DELETE; 3 templates seeded. Verified vs real Postgres: 82 tests (WhatsAppIT ×3: seeded
catalogue, send→ledger→DELIVERED→READ lifecycle, RBAC deny). Fixed a context-load failure:
`@ConditionalOnMissingBean` on the scanned default provider left no bean — switched to plain
`@Component` default + document `@Primary` for the production adapter.

## 2026-07-20 — Module 19: Email Template Engine  ✅ pushed `c014cf4` — MODULE 19 COMPLETE (Phase 8 66%)
`EmailTemplate` entity + Flyway V20; CRUD + preview/render + send via new `EmailService.sendHtml`;
`{placeholder}` rendering; EMAIL:* RBAC; 3 starter templates. 79 tests.

## 2026-07-20 — Module 19: Email Template Engine  ✅ pushed — MODULE 19 COMPLETE (Phase 8 66%)
`EmailTemplate` entity (code/name/category/subject/bodyHtml/active) + Flyway V20; CRUD +
preview/render + send via new `EmailService.sendHtml`; `{placeholder}` rendering reuses
`PlaceholderRenderer`; `renderByCode` programmatic entry for other modules; `EmailTemplateController`
`/api/v1/email-templates` under EMAIL:VIEW/CREATE/EDIT/DELETE; 3 starter templates seeded in
`DataInitializer`. Verified vs real Postgres: 79 tests (EmailTemplateIT ×3: seeded catalogue,
create+preview variable substitution, RBAC deny).

## 2026-07-20 — Module 18: Automation Engine  ✅ pushed — MODULE 18 COMPLETE (Phase 8 33%)
9 configurable rules (birthday/festival/welcome wishes; attendance/leave/confirmation reminders;
missing-documents; promotion congrats; contract expiry). Per-type `AutomationEvaluator` strategy
beans + `AutomationServiceImpl` (EnumMap registry); daily `@Scheduled` job (`SchedulingConfig`
`@EnableScheduling`), idempotent per (rule,date) via `automation_runs` ledger; dispatch through
channel-aware `NotificationService`; `{placeholder}` templating (`PlaceholderRenderer`); manual-run
+ config API `/api/v1/automations` under `NOTIFICATION:VIEW/ADMIN` (D-008). Flyway V19; rules +
required-docs setting seeded in `DataInitializer`. Verified vs real Postgres: 76 tests (AutomationIT
×4: seeded catalogue, birthday dispatch → 2 notifications persisted, rule toggle, RBAC deny).

## 2026-07-20 — Module 17: Reports  ✅ pushed `eb8f335` — MODULE 17 & PHASE 7 COMPLETE
Report types (employee/department/leave/attendance/salary) exported CSV/Excel(POI)/PDF(OpenPDF) via
`ReportRenderer`; `GET /api/v1/reports/{type}?format=&from=&to=` streaming, REPORT:EXPORT RBAC.

## 2026-07-20 — Modules 15 & 16: HR + Employee Dashboards  ✅ (committed) — MODULES 15 & 16 COMPLETE
Dashboard DTOs (CountEntry/PersonDate/HrDashboardResponse/EmployeeDashboardResponse);
DashboardService(+impl) aggregating across modules; DashboardController (/api/v1/dashboard/hr,
/me). Added aggregate repo queries (dept/gender distribution, counts, birthdays via findActiveWithKeyDates).
Employee dashboard composes leave balances, month attendance summary, upcoming holidays, feed,
unread notifications, payslip count, profile-completion %. DASHBOARD:VIEW RBAC. 68 tests green
(added `DashboardIT`).

## (superseded) NEXT UP  → Phase 7, Module 15 — HR Dashboard
- **Next:** Phase 7 (Analytics) — Module 15 HR Dashboard (widgets: headcount, attendance today,
  on-leave, birthdays, work anniversaries, new joiners, pending approvals, dept/gender distribution,
  attrition, etc. — aggregate queries), Module 16 Employee Dashboard (self summary), Module 17
  Reports (employee/attendance/leave/dept/salary; export PDF/Excel/CSV). Mostly read/aggregate.

## 2026-07-20 — Module 14: Employee Lifecycle  ✅ (about to commit) — MODULE 14 & PHASE 6 COMPLETE
LifecycleType/LifecycleStatus enums; LifecycleCase + LifecycleTask entities (one-to-many, cascade)
+ Flyway V18. Repos, DTOs, LifecycleService(+impl), LifecycleController. Initiate ONBOARDING/
OFFBOARDING seeds a default checklist; complete-task auto-advances case to IN_PROGRESS then
COMPLETED when all done; add custom task; cancel; list/get. EMPLOYEE:* RBAC. 66 tests green
(added `LifecycleIT`).

## 2026-07-20 — Module 13: Confirmation & Probation  ✅ (committed) — MODULE 13 COMPLETE
ProbationStatus enum; ProbationRecord entity (separate — confirmation-date field removed from
employee master) + Flyway V17. Repo, DTOs, ProbationService(+impl), ProbationController. Start
(one active per employee), list/get, upcoming-confirmations window, confirm/extend/terminate as
EMPLOYEE:APPROVE decisions; terminate sets employee employmentStatus TERMINATED; notifies employee.
64 tests green (added `ProbationIT`). Authorized under EMPLOYEE:* (no CONFIRMATION module in matrix).

## (superseded) NEXT UP  → Phase 6, Module 13 — Confirmation & Probation
- **Next:** Module 13 — Confirmation & Probation (probation tracking, confirmation, extension,
  HR approval). Then Module 14 — Employee Lifecycle (onboarding/offboarding). Phase 6 — HR Operations.
- Note: Confirmation-Date field was removed from Employee master (project-rules.md), but the
  Confirmation MODULE remains — model probation as a separate entity, not an employee field.

## 2026-07-20 — Module 12: Reimbursement  ✅ (about to commit) — MODULE 12 & PHASE 5 COMPLETE
ExpenseCategory/ReimbursementStatus enums; ReimbursementClaim entity (optional bill-document link)
+ Flyway V16. Repo, DTOs, ReimbursementService(+impl), ReimbursementController. Submit → two-level
manager→finance approval → pay; reject; self-cancel. Rules: no self-approval (defense-in-depth;
note no single role has both EXPENSE:CREATE+APPROVE, so tested with a dual-role EMPLOYEE+FINANCE
user). Notifies employee on decisions. EXPENSE:* RBAC. 62 tests green (added `ReimbursementIT`).

## 2026-07-20 — Module 11: Salary & Compensation  ✅ (committed) — MODULE 11 COMPLETE
PayslipStatus enum; SalaryStructure (revision history) + Payslip entities + Flyway V15; OpenPDF
dependency + PayslipPdfGenerator. Repos, DTOs, SalaryService(+impl), SalaryController. Add revision
(syncs employee current salary snapshot), history/current, generate payslip (snapshot components,
render PDF → StorageService, email attachment via EmailService.sendWithAttachment + in-app notify),
list/get/download. Earnings only (no PF/UAN/ESI/tax per project-rules.md). PAYROLL:* RBAC (Super
Admin lacks CREATE by design). 60 tests green (added `SalaryIT` incl. PDF %PDF check).

## (superseded) NEXT UP  → Phase 5, Module 11 — Salary & Compensation
- **Next:** Module 11 — Salary. Salary structure/components (basic/HRA/allowances/bonus/incentives),
  salary revision history, payslip generation (PDF), download, email; PAYROLL:* RBAC. Note removed
  fields per project-rules.md (no PF/UAN/ESI/tax/professional-tax/loan-deduction). Then Module 12 —
  Reimbursement. (Phase 5 — Finance.)

## 2026-07-20 — Module 10: Notification Center  ✅ (about to commit) — MODULE 10 & PHASE 4 COMPLETE
NotificationChannel/NotificationStatus enums; Notification entity + Flyway V14. Repo (unread count,
mark-all-read @Modifying), DTOs, NotificationService(+impl), NotificationController. Self-service
center (list/unread-count/read/read-all/delete own), admin send (NOTIFICATION:CREATE), retry
(NOTIFICATION:ADMIN). Internal notify() producer API wired into LeaveServiceImpl (approve/reject →
notifies employee). 58 tests green (added `NotificationIT` incl. leave→notification E2E).

## 2026-07-20 — Module 9: Announcement Management  ✅ (committed) — MODULE 9 COMPLETE
AnnouncementCategory enum; Announcement entity + Flyway V13 (optional department target). Repo
(active-feed query), DTOs, AnnouncementService(+impl), AnnouncementController. Draft→publish
lifecycle, publish/expiry window, pinned ordering, active feed. ANNOUNCEMENT:* RBAC — publish uses
APPROVE (Super Admin deliberately lacks it per matrix; HR Manager/Company Admin publish). 56 tests
green (added `AnnouncementIT`).

## 2026-07-20 — Module 8: Holiday Management  ✅ (about to commit) — MODULE 8 & PHASE 3 COMPLETE
HolidayType enum; Holiday entity + Flyway V12 (unique date+name). HolidayRepository, DTOs,
HolidayService(+impl), HolidayController. CRUD + year/range calendar with optional type filter;
duplicate-per-date prevention; HOLIDAY:* RBAC. 54 tests green (added `HolidayIT`).

## 2026-07-20 — Module 7: Leave Management  ✅ (committed) — MODULE 7 COMPLETE
LeaveStatus enum; LeaveType/LeaveBalance/LeaveRequest entities + Flyway V11; 3 repos; 7 DTOs;
LeaveService(+impl); LeaveTypeController + LeaveController. Configurable leave types (seeded 7:
CASUAL/SICK/EARNED/MATERNITY/PATERNITY paid+quota, WFH/LOP untracked). Per-employee balances
(auto-created from quota). Apply→manager→HR two-level approval; balance deducted on final approval,
restored on cancel-of-approved. Rules enforced: balance never negative, no self-approval. Calendar.
LEAVE:* RBAC. 52 tests green (added `LeaveIT`). Fix: ApplyLeaveRequest.halfDay Boolean (Jackson-3
records reject missing primitive booleans).

## (superseded) NEXT UP  → Module 7 — Leave Management (Phase 3)
- **Next:** Module 7 — Leave. Leave types (Casual/Sick/Earned/LOP/WFH/Maternity/Paternity),
  leave balance per employee/type/year, leave request + approval workflow (manager → HR), leave
  calendar, notifications. Business rules: leave balance cannot go negative; employee cannot approve
  own leave (business-rules.md). Then Module 8 — Holiday Management.
- Optional TOTP 2FA (Module 2) deferred — explicitly "Optional" in requirements.

## 2026-07-20 — Module 6: Attendance Management  ✅ (about to commit) — MODULE 6 COMPLETE
Enums AttendanceType(13)/AttendanceSource/ApprovalStatus + AttendanceRecord entity + Flyway V10
(one-per-day unique). OfficeNetworkService (Wi-Fi/IP allowlist w/ exact + IPv4 CIDR; no GPS).
AttendanceService(+impl): check-in (office IP-validated / WFH w/ reason + policy approval), check-out
(working minutes, half-day), my-history, admin list, corrections (upsert), approve/reject, monthly
summary. Reads attendance policy from Module 3 settings. EmployeeRepository.findByUser_Id added.
ATTENDANCE:* RBAC. 49 tests green (added `AttendanceIT`). Fixed Spring-7 rename UNPROCESSABLE_CONTENT.

## 2026-07-20 — Module 5: Document Management  ✅ (about to commit) — MODULE 5 & PHASE 2 COMPLETE
Document entity + Flyway V9; storage abstraction (StorageService + LocalStorageService [path-
traversal safe]; S3 pluggable), StorageProperties, VirusScanner hook (NoOp default). DocumentService
(+impl): content-type allowlist + size validation, virus-scan hook, employee link, folder structure,
expiry; upload (multipart)/list/get/download/preview/delete; DOCUMENT:* RBAC. Binary endpoints stream
outside the ApiResponse envelope. Multipart limits + storage path configured; test storage under
temp dir; /storage/ gitignored. 45 tests green (added `DocumentIT`).

## 2026-07-20 — Module 4 (part 2): Employee child collections + timeline  ✅ (about to commit) — MODULE 4 COMPLETE
Entities EmergencyContact/FamilyMember/EmployeeEducation/EmployeeExperience/EmployeeTimelineEvent +
enum TimelineEventType + Flyway V8 (5 child tables, employee_id FK ON DELETE CASCADE). 5 repos, 10
DTOs, cohesive EmployeeProfileService(+impl), EmployeeProfileController (sub-resources under
/api/v1/employees/{id}, RBAC EMPLOYEE:VIEW/EDIT). Timeline is append-only. 42 tests green (added
`EmployeeProfileIT`).

## 2026-07-20 — Module 4 (part 1): Employee master (scalar record)  ✅ (about to commit)
Employee entity with embeddables (ContactInfo/SalaryInfo/BankDetails/GovernmentIds/SocialProfiles) +
enums (Gender/MaritalStatus/EmploymentStatus) + FKs to Department/Designation/EmploymentType and
optional User link; Flyway V7. Repo (entity graphs), grouped nested DTOs, EmployeeMapper, service
(+impl: unique code, FK resolution, 1:1 user link), controller (/api/v1/employees, EMPLOYEE:*).
Removed fields (reporting-manager/work-location/confirmation-date/skills/certs/github/portfolio/
PF/UAN/ESI/tax) intentionally absent per project-rules.md. 39 tests green (added `EmployeeIT`).

## 2026-07-20 — Module 3 (part 3): Company Profile + System Settings  ✅ (about to commit) — MODULE 3 & PHASE 1 COMPLETE
Entities CompanyProfile (singleton) + SystemSetting (key/value) + Flyway V6; repos, DTOs, mappers,
services(+impl), controllers (/api/v1/company COMPANY:VIEW/EDIT; /api/v1/settings SETTINGS:VIEW/ADMIN).
DataInitializer seeds the company profile + default settings (attendance policies: working hours,
WFH max, office IP allowlist, auto-approve, late grace; general). Non-editable settings protected.
36 tests green (added `CompanySettingsIT`).

## 2026-07-20 — Module 3 (part 2): Employment Types + Locations masters  ✅ (about to commit)
Entities EmploymentType/Location + Flyway V5; repos, DTOs, mappers, services(+impl), controllers
(/api/v1/employment-types, /api/v1/locations). Config masters authorized under SETTINGS:ADMIN
(mutations) / SETTINGS:VIEW (reads) — employees can read reference data, not configure. 34 tests
green (added `OrgConfigIT`).

## 2026-07-20 — Module 3 (part 1): Department + Designation masters  ✅ (about to commit)
Entities Department/Designation + Flyway V4; repos, DTOs (dto/org), mappers, services(+impl),
controllers (/api/v1/departments, /api/v1/designations) with full CRUD, unique code/name,
soft delete, RBAC via DEPARTMENT:*/DESIGNATION:*. 32 tests green (added `OrgMasterIT`).

## 2026-07-20 — Module 2 (part 4): password reset + email verification  ✅ (about to commit)
`AccountToken` entity + Flyway V3; `AccountService(+Impl)`, `AccountController` (public
password-reset/request+confirm, email/verify/request+confirm); `EmailService` (JavaMailSender if
SMTP configured, else logs); `AccountTokenRepository`; token TTL props. Single-use hashed tokens;
reset clears lockout + revokes refresh tokens; no user enumeration. 29 tests green (added
`AccountFlowIT`). **Module 2 mandatory scope complete.**

## 2026-07-20 — Module 2 (part 3): RBAC grant matrix + User Administration  ✅ (about to commit)
`RolePermissionMatrix` (full permissions-matrix.md, 9 roles × 25 modules) applied by `DataInitializer`
to all roles (Super Admin no longer all-perms — matrix-restricted). User admin: `UserService(+Impl)`,
`UserController` (/api/v1/users, @PreAuthorize AUTH/AUTHZ), `UserMapper`, DTOs, `PageResponse`.
Business rule enforced: only Super Admin assigns admin/HR roles; disable/delete revokes tokens.
26 tests green (added `UserAdminIT`, updated `RbacPersistenceIT`).

## Environment notes
- Java 21 · Spring Boot 4.1.0 · Gradle 9.5.1 · PostgreSQL · Docker available (Testcontainers).
- Integration tests use Testcontainers PostgreSQL (not H2). Run: `./gradlew build`.
- Boot-4 gotchas already handled: Flyway autoconfig is in `spring-boot-flyway`; Jackson 3
  (`tools.jackson`); `@WebMvcTest`/`@AutoConfigureMockMvc` in `org.springframework.boot.webmvc.test.autoconfigure`;
  JPA auditing needs an `OffsetDateTime` `DateTimeProvider`.

---

## 2026-07-20 — Module 2 (part 2): JWT authentication  ✅ pushed `4f79aeb`
Stateless `/api/**` JWT chain, login/refresh/logout/me, lockout, login history, RBAC seeding
(175 permissions, 9 roles, Super Admin grant, env-driven bootstrap admin). 23 tests green
(unit + Testcontainers). Files: `security/*`, `service/AuthService(+Impl)`, `controller/AuthController`,
`config/{HrmsSecurityProperties,DataInitializer,SecurityConfig}`, auth DTOs.

## 2026-07-20 — Module 2 (part 1): RBAC & user domain  ✅ pushed `6fd506e`
Enums (RoleName/PermissionAction/PermissionModule), entities (Permission/Role/User/RefreshToken/
LoginHistory), Flyway V2, repositories. Verified vs real Postgres (`validate`).

## 2026-07-20 — Testcontainers PostgreSQL infra  ✅ pushed `748195b`
`AbstractPostgresIntegrationTest`; removed H2 from integration tests.

## 2026-07-19 — Module 1: architecture kernel + doc reconciliation  ✅ pushed `698ff7c`
`ApiResponse<T>`, `GlobalExceptionHandler` (dedicated status handlers), `BaseEntity` + JPA auditing,
`CorrelationIdFilter`, Flyway V1. Decisions D-001..D-007 recorded.
