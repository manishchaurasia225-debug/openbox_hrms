# HRMS — Functional Requirements Specification

> **Document type:** Software Requirements Specification (SRS) — Functional Requirements
> **Product:** OGM Human Resource Management System (HRMS)
> **Document ID:** `01-product/functional-requirements`
> **Status:** Draft v1.0 (Foundation)
> **Owner:** Product Management
> **Audience:** Senior Software Engineers, Product Managers, Architects, QA, Security
> **Last updated:** 2026-07-16
> **Traces to:** [`01-product/vision-and-business-goals`](./vision-and-business-goals.md)

---

## Document Control

| Version | Date       | Author  | Change summary                          |
|--------:|------------|---------|------------------------------------------|
| 1.0     | 2026-07-16 | Product | Initial functional-requirements baseline for all 25 modules |

---

## 1. Introduction

### 1.1 Purpose
This document defines the functional requirements for the OGM HRMS across all planned modules. Each module is specified with a consistent structure so engineering, QA, and product can plan, build, and verify independently while preserving system coherence. It is solution-neutral: it states **what** each module must do, not **how** it is implemented.

### 1.2 How to read this document
Every module follows the same template:

- **Objective** — the business reason the module exists.
- **Functional Requirements (FR)** — testable capabilities, IDed as `FR-<MODULE>-n`.
- **User Stories (US)** — role-centric needs, IDed as `US-<MODULE>-n`.
- **Acceptance Criteria (AC)** — conditions that prove a story/requirement is met (Given/When/Then style).
- **Business Rules (BR)** — invariants and policies the module must enforce, IDed as `BR-<MODULE>-n`.
- **Success Criteria** — measurable outcomes indicating the module delivers value.
- **Dependencies** — other modules this module relies on or is relied upon by.

### 1.3 Requirement conventions
The key words **MUST**, **SHOULD**, and **MAY** are used per RFC 2119. Requirement IDs are stable references; do not renumber once published.

### 1.4 Current implementation status
The current codebase is a **foundation only** (application boots, security scaffold and package structure in place). **No business module below is implemented yet.** Everything here is target scope, delivered incrementally per module.

---

## 2. Module Index

| # | Module | Prefix | Category |
|---|--------|--------|----------|
| 1 | Authentication | `AUTH` | Platform / Security |
| 2 | Authorization (RBAC) | `AUTHZ` | Platform / Security |
| 3 | Company Management | `COMP` | Organization |
| 4 | Branch Management | `BRANCH` | Organization |
| 5 | Department Management | `DEPT` | Organization |
| 6 | Designation Management | `DESIG` | Organization |
| 7 | Employee Management | `EMP` | Core HR |
| 8 | Attendance (Wi-Fi/IP based) | `ATT` | Time & Attendance |
| 9 | Shift Management | `SHIFT` | Time & Attendance |
| 10 | Leave Management | `LEAVE` | Time & Attendance |
| 11 | Holiday Management | `HOL` | Time & Attendance |
| 12 | Payroll | `PAY` | Compensation |
| 13 | Recruitment | `REC` | Talent |
| 14 | Performance Management | `PERF` | Talent |
| 15 | Asset Management | `ASSET` | Operations |
| 16 | Expense & Reimbursement | `EXP` | Finance |
| 17 | Document Management | `DOC` | Operations |
| 18 | Announcements | `ANN` | Communication |
| 19 | Notifications | `NOTIF` | Communication (Infra) |
| 20 | Email Engine | `EMAIL` | Communication (Infra) |
| 21 | WhatsApp Engine | `WA` | Communication (Infra) |
| 22 | AI HR Assistant | `AI` | Intelligence |
| 23 | Reports | `RPT` | Analytics |
| 24 | Dashboard | `DASH` | Analytics |
| 25 | Settings | `SET` | Platform |
| 26 | Audit Logs | `AUDIT` | Platform / Security |

> Note: The module list in the request enumerates 25 named modules; "Audit Logs" is included as module 26 to keep it explicit and independently specifiable.

---

## 3. Cross-Cutting Requirements

These apply to **every** module and are not repeated per module:

- **Multi-tenancy:** Every data operation MUST be scoped to the acting user's company (tenant), except platform-level Super Admin operations. No module may leak data across tenants.
- **RBAC:** Every action MUST be authorized against the permissions matrix (`permissions-matrix.md`).
- **Auditability:** Every create/update/delete on sensitive data MUST emit an audit event (see `AUDIT`).
- **Validation:** All input MUST be validated server-side; violations return structured errors.
- **Soft delete:** Records with historical/financial significance SHOULD be soft-deleted, not physically removed.
- **Internationalization:** User-facing text, dates, numbers, and currency MUST be locale-aware.
- **Notifications:** State changes that concern a user SHOULD raise notifications via `NOTIF`.

---

## 4. Module Specifications

---

### Module 1 — Authentication (`AUTH`)

**Objective:** Verify user identity securely and establish authenticated sessions for all stakeholder types (internal users and external candidates).

**Functional Requirements**
- `FR-AUTH-1` The system MUST allow users to authenticate with an identifier (email/username) and password.
- `FR-AUTH-2` The system MUST issue a secure, stateless access token (JWT) plus a refresh mechanism upon successful login.
- `FR-AUTH-3` The system MUST support logout / token invalidation.
- `FR-AUTH-4` The system MUST support password reset via a verified channel (email link/OTP).
- `FR-AUTH-5` The system MUST support account lockout after a configurable number of failed attempts.
- `FR-AUTH-6` The system SHOULD support multi-factor authentication (MFA) as a configurable policy.
- `FR-AUTH-7` The system SHOULD support single sign-on (SSO/OAuth2/OIDC) as a future integration.

**User Stories**
- `US-AUTH-1` As a user, I want to log in with my credentials so that I can access my authorized features.
- `US-AUTH-2` As a user, I want to reset a forgotten password so that I can regain access without HR intervention.
- `US-AUTH-3` As a security-conscious company, I want to enforce MFA so that accounts are harder to compromise.

**Acceptance Criteria**
- Given valid credentials, when a user logs in, then a valid token is issued and the session is established.
- Given invalid credentials repeated beyond the threshold, when the user tries again, then the account is temporarily locked and the user is informed.
- Given a password-reset request, when the user follows the verified link/OTP, then they can set a new password and old sessions are invalidated.

**Business Rules**
- `BR-AUTH-1` Passwords MUST be stored only as strong one-way hashes; never in plaintext or reversible form.
- `BR-AUTH-2` Tokens MUST expire; refresh tokens MUST be revocable.
- `BR-AUTH-3` Lockout thresholds and MFA policy are company-configurable within platform-set bounds.
- `BR-AUTH-4` Candidates authenticate through a separate, minimally-privileged flow.

**Success Criteria:** Zero plaintext credential storage; successful logins complete within performance budget; measurable reduction in credential-related support tickets.

**Dependencies:** Foundation security scaffold (implemented). Consumed by **all** modules. Uses `EMAIL`/`NOTIF` for reset and MFA. Feeds `AUTHZ`, `AUDIT`.

---

### Module 2 — Authorization / RBAC (`AUTHZ`)

**Objective:** Enforce least-privilege access so every user can perform only the actions their role permits, within their tenant and data scope.

**Functional Requirements**
- `FR-AUTHZ-1` The system MUST evaluate every protected action against a role-based permission model (View/Create/Edit/Delete/Approve/Export/Admin per module).
- `FR-AUTHZ-2` The system MUST scope data access by tenant and by organizational hierarchy (e.g., a Manager sees their reporting line).
- `FR-AUTHZ-3` The system MUST support role assignment and revocation by authorized administrators.
- `FR-AUTHZ-4` The system SHOULD support custom roles and fine-grained permission overrides within platform-defined limits.
- `FR-AUTHZ-5` The system MUST deny by default (no permission ⇒ no access).

**User Stories**
- `US-AUTHZ-1` As a Company Admin, I want to assign roles to users so that access matches responsibility.
- `US-AUTHZ-2` As an Employee, I want to see only my own data so that my privacy and others' are protected.
- `US-AUTHZ-3` As a Manager, I want approval rights over my team only so that authority matches accountability.

**Acceptance Criteria**
- Given a user without a permission, when they attempt the action, then it is denied and audited.
- Given a Manager, when they view leave requests, then they see only their reporting line's requests.
- Given a role change, when it is saved, then the user's effective permissions update on next authorization check.

**Business Rules**
- `BR-AUTHZ-1` Deny-by-default is mandatory; explicit grants only.
- `BR-AUTHZ-2` Tenant isolation overrides all role grants — no role may cross tenants except Super Admin (audited).
- `BR-AUTHZ-3` Data scope (self / team / department / company / platform) is enforced in addition to action permission.

**Success Criteria:** No unauthorized-access incidents; permission checks add negligible latency; audit shows all denials.

**Dependencies:** Depends on `AUTH`. Governs **all** modules. Definitive matrix in `permissions-matrix.md`. Feeds `AUDIT`.

---

### Module 3 — Company Management (`COMP`)

**Objective:** Manage tenant (company) entities and their platform-level configuration, enabling multi-company operation.

**Functional Requirements**
- `FR-COMP-1` The system MUST allow Super Admin to create, configure, suspend, and reactivate companies.
- `FR-COMP-2` The system MUST let each company maintain its profile (legal name, branding, address, tax identifiers, locale, timezone, currency).
- `FR-COMP-3` The system MUST isolate each company's data from every other company.
- `FR-COMP-4` The system SHOULD support company-level policy defaults (leave, attendance, payroll) inherited by sub-entities.

**User Stories**
- `US-COMP-1` As a Super Admin, I want to onboard a new company so that it can begin using the platform.
- `US-COMP-2` As a Company Admin, I want to configure my company profile so that documents and policies reflect our identity.

**Acceptance Criteria**
- Given Super Admin, when a company is created, then an isolated tenant with an initial admin is provisioned.
- Given a suspended company, when its users log in, then access is blocked with an informative message.

**Business Rules**
- `BR-COMP-1` Only Super Admin may create or suspend companies.
- `BR-COMP-2` Company deletion is soft and reversible within a retention window; hard deletion follows a controlled, audited process.
- `BR-COMP-3` Locale, timezone, and currency are mandatory at company creation.

**Success Criteria:** New company onboarding is configuration-only (no engineering); zero cross-tenant leakage.

**Dependencies:** Foundational for **all** company-scoped modules. Depends on `AUTHZ`. Parent of `BRANCH`, `DEPT`, `DESIG`, `EMP`.

---

### Module 4 — Branch Management (`BRANCH`)

**Objective:** Model physical or logical locations within a company (offices, sites, regions) for organization, attendance, and reporting.

**Functional Requirements**
- `FR-BRANCH-1` The system MUST allow authorized users to create/edit/deactivate branches within a company.
- `FR-BRANCH-2` The system MUST let a branch carry attributes (name, address, timezone, allowed network identifiers for attendance).
- `FR-BRANCH-3` The system MUST support assigning employees to a branch.

**User Stories**
- `US-BRANCH-1` As an HR Manager, I want to define branches so that employees and attendance are organized by location.

**Acceptance Criteria**
- Given a company, when a branch is created, then employees can be assigned to it and it appears in scoped reports.

**Business Rules**
- `BR-BRANCH-1` A branch belongs to exactly one company.
- `BR-BRANCH-2` A branch with active employees cannot be deleted, only deactivated.

**Success Criteria:** Location-based organization and reporting function correctly across branches.

**Dependencies:** Depends on `COMP`. Consumed by `EMP`, `ATT` (network identifiers), `SHIFT`, `RPT`.

---

### Module 5 — Department Management (`DEPT`)

**Objective:** Define departments within a company/branch to structure the organization and drive scoping and approvals.

**Functional Requirements**
- `FR-DEPT-1` The system MUST allow create/edit/deactivate of departments.
- `FR-DEPT-2` The system MUST support assigning a department head and mapping employees to departments.
- `FR-DEPT-3` The system SHOULD support department hierarchy (sub-departments).

**User Stories**
- `US-DEPT-1` As an HR Manager, I want to organize employees into departments so that reporting and approvals align to structure.

**Acceptance Criteria**
- Given a department with a head, when a leave request is raised by a member, then the approval routing can use the department structure.

**Business Rules**
- `BR-DEPT-1` A department belongs to one company (and optionally a branch).
- `BR-DEPT-2` A department with active members cannot be deleted, only deactivated.

**Success Criteria:** Organizational structure accurately reflected; approvals and reports scope correctly by department.

**Dependencies:** Depends on `COMP`, `BRANCH`. Consumed by `EMP`, `LEAVE`, `PERF`, `RPT`.

---

### Module 6 — Designation Management (`DESIG`)

**Objective:** Manage job titles/designations and their grade/level metadata used across employee records, payroll grades, and reporting.

**Functional Requirements**
- `FR-DESIG-1` The system MUST allow create/edit/deactivate of designations.
- `FR-DESIG-2` The system MUST support level/grade attributes usable by payroll and performance.
- `FR-DESIG-3` The system MUST support assigning a designation to an employee.

**User Stories**
- `US-DESIG-1` As an HR Manager, I want to define designations so that roles, grades, and pay bands are standardized.

**Acceptance Criteria**
- Given a designation with a grade, when an employee is assigned it, then downstream modules can reference the grade.

**Business Rules**
- `BR-DESIG-1` Designations are company-scoped.
- `BR-DESIG-2` A designation in use cannot be deleted, only deactivated.

**Success Criteria:** Consistent titling and grading across employees; payroll/performance can rely on standardized designations.

**Dependencies:** Depends on `COMP`. Consumed by `EMP`, `PAY`, `PERF`, `REC`, `RPT`.

---

### Module 7 — Employee Management (`EMP`)

**Objective:** Maintain the authoritative employee master record across the full employee lifecycle, serving as the reference data for most other modules.

**Functional Requirements**
- `FR-EMP-1` The system MUST support creating and maintaining employee profiles (personal, contact, employment, statutory, bank details).
- `FR-EMP-2` The system MUST associate each employee with company, branch, department, designation, and reporting manager.
- `FR-EMP-3` The system MUST manage employment status transitions (active, on-notice, suspended, exited).
- `FR-EMP-4` The system MUST support self-service updates to a defined subset of fields (with approval where required).
- `FR-EMP-5` The system MUST store employee documents by reference (see `DOC`).
- `FR-EMP-6` The system MUST maintain a change history for sensitive fields.

**User Stories**
- `US-EMP-1` As an HR Executive, I want to create an employee record so that the person exists across the system.
- `US-EMP-2` As an Employee, I want to update my contact details so that my information stays current.
- `US-EMP-3` As a Manager, I want to view my team's profiles so that I can manage them effectively.

**Acceptance Criteria**
- Given required fields, when an employee is created, then a unique employee identity is established and referenced by other modules.
- Given a self-service update to a restricted field, when submitted, then it enters an approval workflow before taking effect.
- Given an exit is processed, when finalized, then status becomes exited and access is revoked per policy.

**Business Rules**
- `BR-EMP-1` Each employee has a unique, immutable internal identifier within a company.
- `BR-EMP-2` Statutory and bank details are sensitive; access is restricted and all changes audited.
- `BR-EMP-3` Employee records are soft-deleted (never hard-deleted) to preserve payroll/audit history.
- `BR-EMP-4` Reporting-manager cycles are not permitted (no circular reporting).

**Success Criteria:** Single source of truth for employee data; reduced onboarding time (M3); zero orphaned references in dependent modules.

**Dependencies:** Depends on `COMP`, `BRANCH`, `DEPT`, `DESIG`, `AUTH/AUTHZ`. Consumed by nearly all modules: `ATT`, `LEAVE`, `PAY`, `PERF`, `ASSET`, `EXP`, `DOC`, `RPT`.

---

### Module 8 — Attendance / Wi-Fi & IP based (`ATT`)

**Objective:** Capture and reconcile employee attendance automatically, primarily via workplace network (Wi-Fi/IP) validation, minimizing manual correction.

**Functional Requirements**
- `FR-ATT-1` The system MUST allow employees to check in/out, validating presence against approved branch network identifiers (Wi-Fi SSID/BSSID or IP range).
- `FR-ATT-2` The system MUST record attendance events with timestamp, source, and validation result.
- `FR-ATT-3` The system MUST compute daily attendance status (present/absent/half-day/late) from events and applicable shift/policy.
- `FR-ATT-4` The system MUST support manual regularization requests with approval for missed/invalid punches.
- `FR-ATT-5` The system SHOULD support additional sources (biometric feed, geolocation, remote check-in) as configurable options.
- `FR-ATT-6` The system MUST surface exceptions (anomalies, missing punches) for review rather than requiring manual scanning.

**User Stories**
- `US-ATT-1` As an Employee, I want attendance captured automatically when I'm on the office network so that I don't mark it manually.
- `US-ATT-2` As an Employee, I want to raise a regularization when a punch is missed so that my record is corrected fairly.
- `US-ATT-3` As an HR Executive, I want only exceptions surfaced so that I don't review every record.

**Acceptance Criteria**
- Given an employee on an approved branch network, when they check in, then attendance is recorded as validated.
- Given a check-in from an unapproved network, when attempted, then it is flagged/blocked per policy and not silently accepted.
- Given a missed punch, when a regularization is approved, then the day's status updates and payroll uses the corrected value.

**Business Rules**
- `BR-ATT-1` Attendance validity is determined by approved network identifiers configured per branch (`BRANCH`).
- `BR-ATT-2` Attendance status derivation MUST honor the employee's assigned shift (`SHIFT`) and holidays (`HOL`).
- `BR-ATT-3` Manual overrides require approval and are fully audited.
- `BR-ATT-4` Finalized attendance feeding a completed payroll period is locked against edits.

**Success Criteria:** Majority of attendance auto-captured; sharp reduction in manual correction (M1); attendance processing time reduced (M2 dependency).

**Dependencies:** Depends on `EMP`, `BRANCH`, `SHIFT`, `HOL`. Feeds `PAY`, `LEAVE` (LOP), `RPT`, `DASH`.

---

### Module 9 — Shift Management (`SHIFT`)

**Objective:** Define work shifts and rosters so attendance and payroll interpret working hours correctly.

**Functional Requirements**
- `FR-SHIFT-1` The system MUST allow defining shifts (start/end, breaks, grace periods, overnight handling).
- `FR-SHIFT-2` The system MUST support assigning shifts to employees, departments, or branches, including rotating rosters.
- `FR-SHIFT-3` The system MUST expose the effective shift for any employee on any date to `ATT` and `PAY`.

**User Stories**
- `US-SHIFT-1` As an HR Manager, I want to define and assign shifts so that attendance rules match actual working hours.
- `US-SHIFT-2` As a Manager, I want to roster my team across shifts so that coverage is maintained.

**Acceptance Criteria**
- Given a rotating roster, when a date is evaluated, then the correct shift is returned for each employee.
- Given a grace period, when an employee checks in within it, then they are not marked late.

**Business Rules**
- `BR-SHIFT-1` An employee has exactly one effective shift per date.
- `BR-SHIFT-2` Overnight shifts MUST attribute hours to the correct business day per policy.

**Success Criteria:** Attendance and payroll compute correctly for all shift patterns including rotating and overnight.

**Dependencies:** Depends on `EMP`, `DEPT`, `BRANCH`. Consumed by `ATT`, `PAY`, `RPT`.

---

### Module 10 — Leave Management (`LEAVE`)

**Objective:** Manage leave types, balances, accrual, and approval workflows, integrating with attendance and payroll.

**Functional Requirements**
- `FR-LEAVE-1` The system MUST support configurable leave types (paid, sick, casual, unpaid, etc.) with accrual and carry-forward rules.
- `FR-LEAVE-2` The system MUST maintain per-employee leave balances.
- `FR-LEAVE-3` The system MUST let employees apply for leave and track status.
- `FR-LEAVE-4` The system MUST route requests through an approval workflow (manager/HR) with configurable rules.
- `FR-LEAVE-5` The system MUST reflect approved leave in attendance and payroll (including loss-of-pay for unpaid leave).
- `FR-LEAVE-6` The system SHOULD support a team/company leave calendar.

**User Stories**
- `US-LEAVE-1` As an Employee, I want to apply for leave and see my balance so that I can plan time off.
- `US-LEAVE-2` As a Manager, I want to approve or reject leave with visibility into team coverage so that operations aren't disrupted.
- `US-LEAVE-3` As HR, I want unpaid leave to flow into payroll automatically so that pay is accurate.

**Acceptance Criteria**
- Given sufficient balance, when an employee applies, then the request is created and routed for approval, and the balance is provisionally held.
- Given insufficient balance for a paid type, when applying, then the system prevents it or offers an unpaid alternative per policy.
- Given an approval, when granted, then balance is deducted, attendance reflects leave, and payroll uses the outcome.

**Business Rules**
- `BR-LEAVE-1` Applied-but-unapproved leave provisionally holds balance to prevent overdraw.
- `BR-LEAVE-2` Balance never goes negative for paid types unless policy explicitly allows advance leave.
- `BR-LEAVE-3` Leave overlapping holidays/weekends is handled per the leave type's policy.
- `BR-LEAVE-4` Approval authority follows the org hierarchy and RBAC scope.

**Success Criteria:** Leave approval turnaround target met (M4); accurate balances; unpaid leave correctly reflected in payroll.

**Dependencies:** Depends on `EMP`, `DEPT`, `HOL`, `AUTHZ`. Integrates with `ATT`, `PAY`. Uses `NOTIF`. Feeds `RPT`, `DASH`.

---

### Module 11 — Holiday Management (`HOL`)

**Objective:** Maintain holiday calendars (company/branch/region specific) that attendance, leave, and payroll respect.

**Functional Requirements**
- `FR-HOL-1` The system MUST allow defining holiday calendars scoped to company, branch, or region.
- `FR-HOL-2` The system MUST support mandatory and optional (floating) holidays.
- `FR-HOL-3` The system MUST expose applicable holidays to `ATT`, `LEAVE`, and `PAY`.

**User Stories**
- `US-HOL-1` As an HR Manager, I want to maintain holiday calendars so that attendance and payroll account for non-working days.

**Acceptance Criteria**
- Given a holiday on a date, when attendance is computed, then employees are not marked absent for that day.

**Business Rules**
- `BR-HOL-1` Region/branch-specific calendars override the company default where defined.
- `BR-HOL-2` Optional holidays consume from an allowance per policy.

**Success Criteria:** Correct non-working-day handling across all locations; no false absences on holidays.

**Dependencies:** Depends on `COMP`, `BRANCH`. Consumed by `ATT`, `LEAVE`, `PAY`, `RPT`.

---

### Module 12 — Payroll (`PAY`)

**Objective:** Compute accurate, auditable employee compensation from verified attendance, leave, and configured pay rules, producing payslips and disbursement data.

**Functional Requirements**
- `FR-PAY-1` The system MUST define salary structures (earnings, deductions, statutory components) per employee/grade.
- `FR-PAY-2` The system MUST run payroll for a defined cycle, consuming finalized attendance (`ATT`), leave (`LEAVE`), and holidays (`HOL`).
- `FR-PAY-3` The system MUST compute gross, deductions, statutory contributions, and net pay with a transparent breakdown.
- `FR-PAY-4` The system MUST generate payslips per employee.
- `FR-PAY-5` The system MUST support a review/approval step before finalization and a lock after finalization.
- `FR-PAY-6` The system MUST produce disbursement output for the Finance team and record disbursement status.
- `FR-PAY-7` The system MUST retain full payroll history for audit and statutory purposes.

**User Stories**
- `US-PAY-1` As an HR Manager, I want to run and review payroll so that employees are paid correctly and on time.
- `US-PAY-2` As an Employee, I want to view/download my payslip so that I understand my pay.
- `US-PAY-3` As Finance, I want finalized, accurate disbursement data so that I can pay salaries and remit statutory dues.

**Acceptance Criteria**
- Given finalized attendance and leave for a cycle, when payroll runs, then each employee's net pay is computed with an itemized breakdown.
- Given unpaid leave in the period, when computed, then loss-of-pay is applied correctly.
- Given payroll finalization, when approved, then the run is locked and inputs for the period are frozen.

**Business Rules**
- `BR-PAY-1` Payroll MUST consume only finalized/locked attendance and leave for the period.
- `BR-PAY-2` A finalized payroll run is immutable; corrections occur via adjustments in a subsequent cycle, fully audited.
- `BR-PAY-3` All calculations MUST be traceable to inputs and rules (no unexplained figures).
- `BR-PAY-4` Statutory components follow the company's jurisdiction configuration.

**Success Criteria:** Payroll processing time reduced (M2); payroll accuracy trending to ~100% (M8); zero silent miscalculations.

**Dependencies:** Depends on `EMP`, `DESIG`, `ATT`, `LEAVE`, `HOL`, `SHIFT`, `SET`. Feeds `EXP` (if reimbursements paid via payroll), `RPT`, `DASH`, Finance (`EXP`/disbursement). Uses `NOTIF`, `EMAIL`, `DOC` (payslips).

---

### Module 13 — Recruitment (`REC`)

**Objective:** Manage the end-to-end hiring pipeline from requisition to offer, including a candidate-facing experience.

**Functional Requirements**
- `FR-REC-1` The system MUST support job requisitions with approval before publishing.
- `FR-REC-2` The system MUST manage candidates and applications through pipeline stages (applied, screened, interview, offer, hired/rejected).
- `FR-REC-3` The system MUST support interview scheduling and structured feedback collection.
- `FR-REC-4` The system MUST provide a candidate portal to apply, track status, and submit documents.
- `FR-REC-5` The system SHOULD convert a hired candidate into an onboarding employee record (`EMP`).
- `FR-REC-6` The system MAY use AI assistance for JD drafting and candidate summarization (`AI`), with human oversight.

**User Stories**
- `US-REC-1` As a Recruiter, I want to manage candidates through stages so that hiring progresses visibly.
- `US-REC-2` As a Candidate, I want to apply and track my status so that I'm not left in the dark.
- `US-REC-3` As a Hiring Manager, I want to submit interview feedback so that decisions are well-informed.

**Acceptance Criteria**
- Given an approved requisition, when published, then candidates can apply via the portal.
- Given a candidate marked hired, when converted, then an onboarding employee record is created without re-entering data.

**Business Rules**
- `BR-REC-1` Candidate data is minimized and access-restricted; retention follows policy.
- `BR-REC-2` Requisitions require approval before becoming public.
- `BR-REC-3` AI-assisted screening MUST NOT auto-reject; humans decide.

**Success Criteria:** Time-to-hire reduced (M9); improved pipeline visibility and candidate experience.

**Dependencies:** Depends on `DEPT`, `DESIG`, `AUTHZ`. Integrates with `EMP` (conversion), `DOC`, `AI`, `EMAIL`/`WA`/`NOTIF`.

---

### Module 14 — Performance Management (`PERF`)

**Objective:** Run structured performance cycles (goals, reviews, feedback) to support development and decisions.

**Functional Requirements**
- `FR-PERF-1` The system MUST support goal/objective setting per employee and cycle.
- `FR-PERF-2` The system MUST support review cycles (self, manager, and optionally peer/360) with ratings and comments.
- `FR-PERF-3` The system MUST route reviews through the org hierarchy and record outcomes.
- `FR-PERF-4` The system SHOULD summarize reviews with AI assistance (`AI`) under human oversight.

**User Stories**
- `US-PERF-1` As an Employee, I want to set goals and complete self-review so that my contribution is recognized.
- `US-PERF-2` As a Manager, I want to review my team so that I can guide development and inform decisions.

**Acceptance Criteria**
- Given an active cycle, when an employee submits a self-review, then it routes to the manager for review and finalization.

**Business Rules**
- `BR-PERF-1` Review visibility follows RBAC and hierarchy (employees see their own; managers see reports).
- `BR-PERF-2` AI summaries are advisory; ratings are human-authored.

**Success Criteria:** Structured, on-time review cycles; outcomes traceable and available to authorized roles.

**Dependencies:** Depends on `EMP`, `DEPT`, `DESIG`, `AUTHZ`. Uses `AI`, `NOTIF`. Feeds `RPT`, `DASH`.

---

### Module 15 — Asset Management (`ASSET`)

**Objective:** Track company assets and their assignment to employees through issue and return.

**Functional Requirements**
- `FR-ASSET-1` The system MUST maintain an asset inventory (type, identifier, status, value, condition).
- `FR-ASSET-2` The system MUST support assigning/returning assets to/from employees with a history.
- `FR-ASSET-3` The system MUST surface outstanding assets during employee offboarding.

**User Stories**
- `US-ASSET-1` As an HR Executive, I want to assign assets to employees so that accountability is clear.
- `US-ASSET-2` As HR, I want outstanding assets flagged at exit so that recovery happens before final settlement.

**Acceptance Criteria**
- Given an employee with assigned assets, when offboarding starts, then unreturned assets are listed and block completion per policy.

**Business Rules**
- `BR-ASSET-1` An asset is assigned to at most one employee at a time.
- `BR-ASSET-2` Asset history is immutable and auditable.

**Success Criteria:** Full traceability of assets; reduced loss; clean offboarding recovery.

**Dependencies:** Depends on `EMP`. Integrates with offboarding (`EMP`), `DASH`, `RPT`, `NOTIF`.

---

### Module 16 — Expense & Reimbursement (`EXP`)

**Objective:** Let employees claim expenses and route them through approval and reimbursement, integrating with finance/payroll.

**Functional Requirements**
- `FR-EXP-1` The system MUST let employees submit expense claims with receipts (`DOC`) and categories.
- `FR-EXP-2` The system MUST route claims through approval (manager/finance) per policy and limits.
- `FR-EXP-3` The system MUST record reimbursement status and integrate payout with finance/payroll.
- `FR-EXP-4` The system MUST enforce policy limits and flag violations.

**User Stories**
- `US-EXP-1` As an Employee, I want to submit an expense claim with receipts so that I'm reimbursed.
- `US-EXP-2` As Finance, I want approved claims with clear data so that I can reimburse accurately.

**Acceptance Criteria**
- Given a claim within policy, when approved, then it moves to reimbursement and the employee is notified.
- Given a claim exceeding limits, when submitted, then it is flagged and requires higher approval or is rejected.

**Business Rules**
- `BR-EXP-1` Claims require valid supporting documents where policy mandates.
- `BR-EXP-2` Approval authority and limits follow RBAC and policy configuration.
- `BR-EXP-3` Reimbursed claims are locked and auditable.

**Success Criteria:** Faster, transparent reimbursements; policy compliance; clean finance integration.

**Dependencies:** Depends on `EMP`, `DOC`, `AUTHZ`, `SET` (policy). Integrates with `PAY`/Finance. Uses `NOTIF`. Feeds `RPT`.

---

### Module 17 — Document Management (`DOC`)

**Objective:** Securely store, version, and control access to HR documents (employee, payroll, recruitment, expense) with approval where needed.

**Functional Requirements**
- `FR-DOC-1` The system MUST store documents securely with metadata, ownership, and access control.
- `FR-DOC-2` The system MUST support document categories and, where required, an approval workflow (e.g., policy acknowledgements).
- `FR-DOC-3` The system SHOULD support versioning and expiry/renewal reminders (e.g., visas, certifications).
- `FR-DOC-4` The system MUST restrict document access by RBAC and ownership.

**User Stories**
- `US-DOC-1` As an Employee, I want to upload and view my documents so that my records are complete.
- `US-DOC-2` As HR, I want to require acknowledgement of policy documents so that compliance is recorded.

**Acceptance Criteria**
- Given a restricted document, when an unauthorized user requests it, then access is denied and audited.
- Given a document requiring acknowledgement, when the employee acknowledges, then the acknowledgement is recorded with timestamp.

**Business Rules**
- `BR-DOC-1` Sensitive documents are access-controlled and encrypted at rest.
- `BR-DOC-2` Document deletion is soft where retention/audit requires.

**Success Criteria:** Centralized, secure, auditable document handling; expiry reminders reduce lapses.

**Dependencies:** Consumed by `EMP`, `PAY` (payslips), `REC`, `EXP`. Depends on `AUTHZ`, `AUDIT`. Uses `NOTIF`.

---

### Module 18 — Announcements (`ANN`)

**Objective:** Broadcast company/branch/department-scoped announcements to targeted audiences.

**Functional Requirements**
- `FR-ANN-1` The system MUST let authorized users create announcements targeted by company/branch/department/role.
- `FR-ANN-2` The system MUST publish announcements to recipients' feeds and optionally via `NOTIF`/`EMAIL`/`WA`.
- `FR-ANN-3` The system SHOULD support scheduling and expiry of announcements.

**User Stories**
- `US-ANN-1` As an HR Manager, I want to publish an announcement to a target audience so that the right people are informed.
- `US-ANN-2` As an Employee, I want to see relevant announcements so that I stay informed.

**Acceptance Criteria**
- Given a targeted audience, when an announcement is published, then only that audience sees it and optional channels deliver it.

**Business Rules**
- `BR-ANN-1` Announcement authoring authority follows RBAC and scope.
- `BR-ANN-2` Targeting MUST respect tenant and scope boundaries.

**Success Criteria:** Timely, correctly-targeted communication; measurable read/engagement where tracked.

**Dependencies:** Depends on `COMP`, `BRANCH`, `DEPT`, `AUTHZ`. Uses `NOTIF`, `EMAIL`, `WA`.

---

### Module 19 — Notifications (`NOTIF`)

**Objective:** Provide a unified, channel-agnostic notification service delivering in-app and external alerts for system events.

**Functional Requirements**
- `FR-NOTIF-1` The system MUST let modules raise notifications for events (approvals, status changes, reminders).
- `FR-NOTIF-2` The system MUST deliver in-app notifications and dispatch to external channels (`EMAIL`, `WA`) per user/company preference.
- `FR-NOTIF-3` The system MUST support user notification preferences and read/unread state.
- `FR-NOTIF-4` The system SHOULD support templated, localized notification content.

**User Stories**
- `US-NOTIF-1` As a user, I want to be notified of actions requiring me so that nothing is missed.
- `US-NOTIF-2` As a user, I want to control my notification preferences so that I'm not overwhelmed.

**Acceptance Criteria**
- Given an approval assigned to a user, when raised, then the user receives a notification via their preferred channel(s).

**Business Rules**
- `BR-NOTIF-1` Notifications respect user preferences and quiet-hours where configured.
- `BR-NOTIF-2` Delivery failures are logged and retried per policy.

**Success Criteria:** Reliable, preference-aware delivery; reduced missed approvals; auditable dispatch.

**Dependencies:** Consumed by nearly all modules. Uses `EMAIL`, `WA`. Depends on `EMP` (recipients), `SET`.

---

### Module 20 — Email Engine (`EMAIL`)

**Objective:** Provide reliable transactional and templated email delivery for the platform.

**Functional Requirements**
- `FR-EMAIL-1` The system MUST send transactional emails (auth, approvals, payslips, offers) reliably.
- `FR-EMAIL-2` The system MUST support localized, branded templates per company.
- `FR-EMAIL-3` The system MUST record delivery outcomes and support retries.
- `FR-EMAIL-4` The system SHOULD support company-configurable sender identity within platform controls.

**User Stories**
- `US-EMAIL-1` As HR, I want system emails to reflect our brand so that communication is professional.

**Acceptance Criteria**
- Given a triggering event, when an email is dispatched, then it uses the correct template/locale and its outcome is recorded.

**Business Rules**
- `BR-EMAIL-1` Email content follows templating and localization standards; no ad-hoc unversioned content for critical flows.
- `BR-EMAIL-2` Bounces/failures are tracked and surfaced.

**Success Criteria:** High deliverability; branded, localized communication; auditable dispatch.

**Dependencies:** Foundation includes `spring-boot-starter-mail`. Consumed by `NOTIF`, `AUTH`, `PAY`, `REC`, `ANN`, `EXP`.

---

### Module 21 — WhatsApp Engine (`WA`)

**Objective:** Deliver notifications and lightweight interactions over WhatsApp as an optional channel, respecting consent and provider rules.

**Functional Requirements**
- `FR-WA-1` The system MUST send templated WhatsApp messages via an approved provider integration for opted-in recipients.
- `FR-WA-2` The system MUST record delivery status and handle provider constraints (template approval, rate limits).
- `FR-WA-3` The system MUST honor explicit opt-in/opt-out consent.
- `FR-WA-4` The system SHOULD support basic inbound interactions (e.g., approval acknowledgements) where feasible.

**User Stories**
- `US-WA-1` As an Employee, I want optional WhatsApp alerts so that I get timely updates on my preferred channel.

**Acceptance Criteria**
- Given an opted-in recipient, when a notification targets WhatsApp, then an approved template is delivered and status recorded.
- Given no consent, when WhatsApp delivery is attempted, then it is suppressed and the event logged.

**Business Rules**
- `BR-WA-1` Messaging requires explicit recipient consent.
- `BR-WA-2` Only provider-approved templates are used; content/localization standards apply.

**Success Criteria:** Compliant, consent-based WhatsApp delivery; improved reach for time-sensitive alerts.

**Dependencies:** Consumed by `NOTIF`, `ANN`, `REC`. Depends on `SET` (provider config, consent), `EMP` (contact/consent).

---

### Module 22 — AI HR Assistant (`AI`)

**Objective:** Provide scoped, human-supervised AI assistance that reduces HR effort and error without making autonomous decisions about people.

**Functional Requirements**
- `FR-AI-1` The system MUST provide a conversational assistant answering HR/policy questions from authorized, tenant-scoped data.
- `FR-AI-2` The system MUST assist with drafting (job descriptions, letters, summaries) with human review before use.
- `FR-AI-3` The system SHOULD detect and flag anomalies (attendance/payroll) for human review.
- `FR-AI-4` The system MUST label AI-generated content and record that human oversight occurred for consequential actions.
- `FR-AI-5` The system MUST enforce data minimization and tenant isolation for all AI operations.

**User Stories**
- `US-AI-1` As an Employee, I want to ask policy questions and get accurate answers so that I don't wait on HR.
- `US-AI-2` As a Recruiter, I want an AI-drafted JD so that I start faster, then edit it myself.
- `US-AI-3` As HR, I want anomalies flagged so that I catch issues early.

**Acceptance Criteria**
- Given a policy question within the user's scope, when asked, then the assistant answers from authorized data and cites/labels appropriately.
- Given AI-drafted content, when produced, then it is clearly labeled and requires human confirmation before formal use.

**Business Rules**
- `BR-AI-1` AI MUST NOT autonomously decide hiring, pay, discipline, or termination.
- `BR-AI-2` AI operations MUST respect RBAC, tenant isolation, and data minimization.
- `BR-AI-3` AI outputs affecting individuals MUST be human-reviewed and auditable.

**Success Criteria:** Measurable HR time saved with no loss of accuracy, fairness, or compliance; positive user satisfaction.

**Dependencies:** Depends on `AUTHZ`, `EMP`, `SET`, and relevant module data (`LEAVE`, `PAY`, `ATT`, `REC`, `PERF`). Governed by `AUDIT`.

---

### Module 23 — Reports (`RPT`)

**Objective:** Provide role-appropriate, exportable reports across all HR domains for operational and compliance needs.

**Functional Requirements**
- `FR-RPT-1` The system MUST provide standard reports per module (headcount, attendance, leave, payroll, hiring, assets, expenses).
- `FR-RPT-2` The system MUST scope report data by RBAC and tenant.
- `FR-RPT-3` The system MUST support export (e.g., CSV/PDF) with the Export permission.
- `FR-RPT-4` The system SHOULD support filters, date ranges, and scheduled report generation.

**User Stories**
- `US-RPT-1` As an HR Manager, I want operational reports so that I can manage and comply.
- `US-RPT-2` As Finance, I want payroll/expense reports so that I can reconcile and report.

**Acceptance Criteria**
- Given a user with Export permission, when they export a report, then only data within their scope is included.

**Business Rules**
- `BR-RPT-1` Reports never expose data beyond the requester's authorized scope.
- `BR-RPT-2` Exports are audited.

**Success Criteria:** Decisions made on live, trustworthy data; reduced ad-hoc reporting requests to HR.

**Dependencies:** Reads from most modules. Depends on `AUTHZ`, `AUDIT`.

---

### Module 24 — Dashboard (`DASH`)

**Objective:** Present role-appropriate, real-time summaries and KPIs as each user's landing view.

**Functional Requirements**
- `FR-DASH-1` The system MUST present role-specific dashboards (e.g., Employee self-summary, Manager team view, HR operations, Finance payroll status).
- `FR-DASH-2` The system MUST surface pending actions (approvals, tasks) and key metrics.
- `FR-DASH-3` The system MUST scope all widgets by RBAC and tenant.

**User Stories**
- `US-DASH-1` As a user, I want a dashboard summarizing what needs my attention so that I act efficiently.

**Acceptance Criteria**
- Given a Manager, when they open the dashboard, then they see pending approvals and team metrics for their scope only.

**Business Rules**
- `BR-DASH-1` Dashboard widgets respect the same data scoping as their source modules.

**Success Criteria:** High engagement; users act on pending items faster; reduced missed tasks.

**Dependencies:** Aggregates from `ATT`, `LEAVE`, `PAY`, `REC`, `PERF`, `EXP`, `ASSET`, `NOTIF`. Depends on `AUTHZ`.

---

### Module 25 — Settings (`SET`)

**Objective:** Provide platform- and company-level configuration (policies, integrations, preferences) that other modules consume.

**Functional Requirements**
- `FR-SET-1` The system MUST provide company-level settings (policies for leave/attendance/payroll/expense, locale, branding, integrations).
- `FR-SET-2` The system MUST provide platform-level settings for Super Admin (global defaults, security baselines).
- `FR-SET-3` The system MUST validate settings changes and version them for audit.
- `FR-SET-4` The system MUST expose effective settings to consuming modules.

**User Stories**
- `US-SET-1` As a Company Admin, I want to configure policies so that the system enforces our rules.
- `US-SET-2` As a Super Admin, I want to set global security baselines so that all tenants meet a minimum bar.

**Acceptance Criteria**
- Given a company policy change, when saved, then dependent modules apply the new policy going forward, and the change is audited.

**Business Rules**
- `BR-SET-1` Company settings cannot weaken platform-mandated security baselines.
- `BR-SET-2` Settings changes are versioned and audited.

**Success Criteria:** Company onboarding is configuration-driven; consistent policy enforcement; no unaudited setting changes.

**Dependencies:** Consumed by virtually all modules. Depends on `AUTHZ`, `AUDIT`.

---

### Module 26 — Audit Logs (`AUDIT`)

**Objective:** Record an immutable trail of security- and data-sensitive events for compliance, forensics, and accountability.

**Functional Requirements**
- `FR-AUDIT-1` The system MUST record actor, action, target, timestamp, tenant, and outcome for sensitive operations across all modules.
- `FR-AUDIT-2` The system MUST make audit records immutable (append-only) and tamper-evident.
- `FR-AUDIT-3` The system MUST let authorized roles search/filter/export audit logs within scope.
- `FR-AUDIT-4` The system MUST record authorization denials and administrative actions.

**User Stories**
- `US-AUDIT-1` As a Company Admin, I want to review who changed sensitive data so that I can ensure accountability.
- `US-AUDIT-2` As a Super Admin, I want platform-level audit visibility so that I can investigate incidents.

**Acceptance Criteria**
- Given a sensitive change, when it occurs, then an immutable audit record is written and is later retrievable by authorized users within scope.

**Business Rules**
- `BR-AUDIT-1` Audit records are never editable or deletable through application flows.
- `BR-AUDIT-2` Audit access is itself restricted and audited.
- `BR-AUDIT-3` Retention follows compliance policy.

**Success Criteria:** Complete, tamper-evident traceability of sensitive actions; supports compliance and investigations.

**Dependencies:** Fed by **all** modules. Depends on `AUTHZ`. Consumed by `RPT`.

---

## 5. Inter-Module Dependency Summary

The table shows primary dependencies (**Depends on**) and principal consumers (**Feeds**). Cross-cutting infra (`AUTH`, `AUTHZ`, `NOTIF`, `AUDIT`, `SET`) touches most modules and is abbreviated.

| Module | Depends on | Feeds / Consumed by |
|--------|-----------|---------------------|
| `AUTH` | Foundation security | All modules, `AUTHZ`, `AUDIT` |
| `AUTHZ` | `AUTH` | All modules, `AUDIT` |
| `COMP` | `AUTHZ` | `BRANCH`, `DEPT`, `DESIG`, `EMP`, all company-scoped modules |
| `BRANCH` | `COMP` | `EMP`, `ATT`, `SHIFT`, `HOL`, `RPT` |
| `DEPT` | `COMP`, `BRANCH` | `EMP`, `LEAVE`, `PERF`, `RPT` |
| `DESIG` | `COMP` | `EMP`, `PAY`, `PERF`, `REC` |
| `EMP` | `COMP`, `BRANCH`, `DEPT`, `DESIG` | `ATT`, `LEAVE`, `PAY`, `PERF`, `ASSET`, `EXP`, `DOC`, `RPT` |
| `ATT` | `EMP`, `BRANCH`, `SHIFT`, `HOL` | `PAY`, `LEAVE`, `RPT`, `DASH` |
| `SHIFT` | `EMP`, `DEPT`, `BRANCH` | `ATT`, `PAY`, `RPT` |
| `LEAVE` | `EMP`, `DEPT`, `HOL` | `ATT`, `PAY`, `RPT`, `DASH` |
| `HOL` | `COMP`, `BRANCH` | `ATT`, `LEAVE`, `PAY` |
| `PAY` | `EMP`, `DESIG`, `ATT`, `LEAVE`, `HOL`, `SHIFT`, `SET` | `RPT`, `DASH`, Finance/`EXP`, `DOC`, `EMAIL` |
| `REC` | `DEPT`, `DESIG` | `EMP`, `DOC`, `AI`, `EMAIL`/`WA` |
| `PERF` | `EMP`, `DEPT`, `DESIG` | `RPT`, `DASH`, `AI` |
| `ASSET` | `EMP` | offboarding (`EMP`), `RPT`, `DASH` |
| `EXP` | `EMP`, `DOC`, `SET` | `PAY`/Finance, `RPT` |
| `DOC` | `AUTHZ`, `AUDIT` | `EMP`, `PAY`, `REC`, `EXP` |
| `ANN` | `COMP`, `BRANCH`, `DEPT` | `NOTIF`, `EMAIL`, `WA` |
| `NOTIF` | `EMP`, `SET` | All modules; uses `EMAIL`, `WA` |
| `EMAIL` | Foundation mail | `NOTIF`, `AUTH`, `PAY`, `REC`, `ANN`, `EXP` |
| `WA` | `SET`, `EMP` (consent) | `NOTIF`, `ANN`, `REC` |
| `AI` | `AUTHZ`, `EMP`, `SET`, module data | `REC`, `PERF`, `ATT`, `PAY` (advisory), `AUDIT` |
| `RPT` | Most modules, `AUTHZ`, `AUDIT` | Users, Finance, Leadership |
| `DASH` | Most modules, `AUTHZ` | All roles |
| `SET` | `AUTHZ`, `AUDIT` | Virtually all modules |
| `AUDIT` | `AUTHZ` | `RPT`; fed by all modules |

### 5.1 Recommended build sequence (from dependencies)
1. **Platform/Security:** `AUTH` → `AUTHZ` → `SET` → `AUDIT`
2. **Organization:** `COMP` → `BRANCH` → `DEPT` → `DESIG`
3. **Core HR:** `EMP`
4. **Communication infra:** `EMAIL` → `NOTIF` (→ `WA`)
5. **Time & Attendance:** `SHIFT` → `HOL` → `ATT` → `LEAVE`
6. **Compensation:** `PAY`
7. **Talent & Operations:** `REC`, `PERF`, `ASSET`, `EXP`, `DOC`, `ANN`
8. **Intelligence & Analytics:** `AI`, `RPT`, `DASH`

---

## 6. Global Success Criteria

The functional set is successful when the business goals and metrics in the vision document are met — chiefly: reduced manual HR work (M1), faster attendance and payroll (M2), shorter onboarding (M3), faster leave turnaround (M4), high self-service adoption (M7), and payroll accuracy (M8) — all achieved without violating the non-functional and security constraints defined in the vision.

---

*End of document. Living document; changes follow the document-control process and must stay consistent with the vision, permissions matrix, glossary, and subsequent Engineering Bible documents.*
