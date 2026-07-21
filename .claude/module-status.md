# Module Status

> Source of truth for what is actually built. Updated as each module lands. Legend:
> ✅ Complete · 🟡 In Progress · ⬜ Not Started.
> (Corrected 2026-07-19 — the previous statuses were template placeholders, not reality.)

## Phase 1 — Foundation

| Module | Status | Notes |
|--------|--------|-------|
| Foundation shell (security, exception handling, home) | ✅ | `SecurityConfig`, `GlobalExceptionHandler`, `ApiResponse`, `HomeController` |
| Module 1 — System/architecture kernel | ✅ | `ApiResponse<T>`, `BaseEntity`+auditing, Flyway, request-id, Testcontainers |
| Module 2 — Authentication & Authorization (JWT + dynamic RBAC) | ✅ | JWT login/refresh/logout/me, lockout, login history, full RBAC grant matrix (9 roles × 25 modules), user administration, password reset, email verification. Optional TOTP 2FA deferred (explicitly "Optional" in spec) |
| Module 3 — Organization & System Settings | ✅ | Departments, Designations, Employment Types, Locations, Company profile (singleton), System Settings (key/value incl. seeded attendance policies). **Phase 1 complete.** |

## Phase 2 — Employee Core

| Module | Status |
|--------|--------|
| Module 4 — Employee Management | ✅ (master + contact/salary/bank/gov-IDs/social embeddables, org FKs, user link; child collections: emergency contacts, family, education, experience; append-only timeline) |
| Module 5 — Document Management | ✅ (storage abstraction [local FS; S3 pluggable], upload/download/preview/list/delete, content-type + size validation, virus-scan hook, metadata + expiry, RBAC DOCUMENT:*). **Phase 2 complete.** |

## Phase 3 — Attendance & Leave

| Module | Status |
|--------|--------|
| Module 6 — Attendance (Wi-Fi/IP, no GPS) | ✅ (check-in/out, office Wi-Fi/IP validation vs allowlist setting, WFH workflow + policy-driven approval, corrections, approvals, my-history, admin list, monthly summary; ATTENDANCE:* RBAC) |
| Module 7 — Leave | ✅ (configurable leave types [seeded], per-employee balances, apply → two-level manager→HR approval, reject, self-cancel + balance restore, calendar; rules: balance never negative, no self-approval; LEAVE:* RBAC) |
| Module 8 — Holiday | ✅ (holiday entity [national/regional/company, region, recurring], CRUD, year/range calendar with type filter, duplicate-per-date prevention; HOLIDAY:* RBAC). **Phase 3 complete.** |

## Phase 4–10

| Module | Status |
|--------|--------|
| 9 Announcements | ✅ |
| 10 Notifications | ✅ (in-app center: list/unread-count/read/read-all/delete; admin send; retry; channels IN_APP/EMAIL/WHATSAPP/PUSH; producer API wired to leave approval → notifies employee) — **Phase 4 complete** |
| 11 Salary | ✅ (salary revision history + current-sync to employee, payslip generation with PDF [OpenPDF] into storage, download, email attachment + in-app notify, no statutory deductions; PAYROLL:* RBAC — Super Admin lacks CREATE by design) |
| 12 Reimbursement | ✅ (expense categories, claim submit with optional bill-document link, two-level manager→finance approval, reject, pay, self-cancel; EXPENSE:* RBAC; no-self-approval rule; notifies employee) — **Phase 5 complete** |
| 13 Confirmation & Probation | ✅ (probation entity [separate — confirmation-date field removed], start/list/get/upcoming, confirm/extend/terminate as EMPLOYEE:APPROVE actions, one-active-per-employee, notifies employee) |
| 14 Employee Lifecycle | ✅ (onboarding/offboarding cases with seeded checklists, complete-task → auto IN_PROGRESS/COMPLETED, add custom task, cancel, list/get; EMPLOYEE:* RBAC) — **Phase 6 complete** |
| 15 HR Dashboard | ✅ (aggregate widgets: headcount, present-today, on-leave, pending approvals, new joiners, upcoming birthdays/anniversaries, department & gender distribution; DASHBOARD:VIEW) |
| 16 Employee Dashboard | ✅ (self summary: profile completion, month attendance summary, leave balances, upcoming holidays, announcements feed, unread notifications, payslip count) |
| 17 Reports | ✅ (export EMPLOYEE/DEPARTMENT/LEAVE/ATTENDANCE/SALARY as CSV/Excel[POI]/PDF[OpenPDF]; GET /api/v1/reports/{type}?format=&from=&to= streaming download; REPORT:EXPORT RBAC) — **Phase 7 complete** |
| 18 Automation | ✅ (Automation Engine: 9 configurable rules [birthday/festival/welcome wishes, attendance/leave/confirmation reminders, missing-documents, promotion congrats, contract expiry]; per-type `AutomationEvaluator` strategy; daily `@Scheduled` job [`@EnableScheduling`], idempotent per (rule,date) via run ledger; dispatch through channel-aware `NotificationService`; `{placeholder}` templating; manual run + config API under `NOTIFICATION:VIEW/ADMIN` [D-008]; seeded rules + required-docs setting) |
| 19 Email Templates | ✅ (Email Template Engine: HTML templates with categories + dynamic `{placeholder}` variables; CRUD + preview/render + send; `EmailTemplate` entity/Flyway V20; `EmailService.sendHtml` added; render-by-code programmatic entry for other modules; 3 starter templates seeded; EMAIL:VIEW/CREATE/EDIT/DELETE RBAC) |
| 20 WhatsApp | ✅ (WhatsApp Integration: pluggable `WhatsAppProvider` adapter + `LoggingWhatsAppProvider` no-op default [D-009]; WhatsApp templates [Meta category taxonomy] CRUD + preview; send → `whatsapp_messages` ledger; delivery/read lifecycle QUEUED→SENT→DELIVERED→READ/FAILED via status-update endpoint [Meta webhook stand-in]; Flyway V21; seeded templates; WHATSAPP:VIEW/CREATE/EDIT/DELETE RBAC) — **Phase 8 complete** |
| 21 AI Assistant | ✅ (Enterprise AI Assistant: `AiTool` tool-calling layer over **services** [employee search, attendance search, report generation]; swappable `AiAssistantProvider` with deterministic offline `RuleBasedAiAssistantProvider` default [D-010]; permission-filtered — tools restricted to caller authorities, no escalation; `/api/v1/ai/assistant` + `/tools` under AI:VIEW + per-tool module authority; stateless, no migration) |
| 22 Global Search | ✅ (unified `/api/v1/search?q=`: pluggable `EntitySearcher` per type [employees, departments, designations]; permission-filtered — a type is searched only if the caller holds its module VIEW authority; typed grouped results; repo search finders added; authenticated endpoint) — **Phase 9 complete** |
| 23 Audit Logs | ✅ (append-only `audit_logs` ledger; `AuditService` producer [REQUIRES_NEW, best-effort]; `AuditInterceptor` captures mutations + downloads generically [no service changes]; explicit login/logout/failed-login capture in AuthService; filtered query API via JPA Specifications; Flyway V22; AUDIT:VIEW RBAC — D-011) |
| 24 System Administration | ✅ (admin console API `/api/v1/admin`: system info/health [app info, uptime, DB status, live entity counts], role→permission catalogue from canonical matrix, login-history access; composes existing services — settings/user admin not duplicated; SETTINGS:ADMIN / AUTHZ:VIEW / AUDIT:VIEW RBAC) — **Phase 10 & ALL MODULES COMPLETE** |

> Removed from scope per `project-rules.md` (D-006): Shift Management, GPS attendance, document
> version history, PF/UAN/ESI/tax fields, and other listed removals. Multi-tenancy deferred (D-004).
