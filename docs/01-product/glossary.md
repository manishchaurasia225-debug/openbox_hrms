# HRMS — Glossary of Terms

> **Document type:** Reference / Ubiquitous Language
> **Product:** OGM Human Resource Management System (HRMS)
> **Document ID:** `01-product/glossary`
> **Status:** Draft v1.0 (Foundation)
> **Owner:** Product Management
> **Audience:** All contributors (Engineering, Product, QA, Security, Design)
> **Last updated:** 2026-07-16
> **Traces to:** all `01-product/*` documents

---

## Document Control

| Version | Date       | Author  | Change summary            |
|--------:|------------|---------|----------------------------|
| 1.0     | 2026-07-16 | Product | Initial glossary baseline  |

---

## 1. Purpose

This glossary defines the **ubiquitous language** of the HRMS — the shared vocabulary that domain experts, product, and engineering all use with the same meaning. Terms defined here MUST be used consistently in code (entities, services, APIs), documentation, and UI. Where a term maps to a module, the module prefix is noted (see [`functional-requirements`](./functional-requirements.md)).

**Reading aids**
- **Also known as (AKA)** lists common synonyms — prefer the primary term.
- **Related** links conceptually adjacent terms.
- Definitions are organizational/product-level and jurisdiction-neutral; statutory specifics are configured per company (`SET`).

---

## 2. Organizational Structure

**Company** *(module: `COMP`)*
A tenant on the platform — a distinct legal/business organization whose data is fully isolated from all other companies. The top of the organizational hierarchy. *Related:* Tenant, Multi-Tenancy, Branch.

**Tenant**
The isolation boundary for a Company's data on the shared platform. Every company-scoped record belongs to exactly one tenant; no data crosses tenants except through audited Super Admin operations. *AKA:* Company (in a data-isolation context).

**Multi-Tenancy**
The architectural property that lets many Companies operate on shared infrastructure with strict per-tenant data isolation and independent configuration. *Related:* Tenant, Company.

**Branch** *(module: `BRANCH`)*
A physical or logical location within a Company (e.g., office, site, region). Carries attributes such as address, timezone, and the approved network identifiers used for attendance validation. *Related:* Company, Department, Attendance.

**Department** *(module: `DEPT`)*
An organizational unit within a Company (and optionally a Branch) grouping employees by function, with an assigned department head. Drives data scoping and approval routing. *Related:* Designation, Reporting Manager.

**Designation** *(module: `DESIG`)*
A job title/role assigned to an employee, optionally carrying a grade/level used by payroll and performance. *AKA:* Job Title, Role (organizational, not RBAC). *Related:* Grade, Employee, Payroll.

**Grade / Level**
A ranking attribute associated with a Designation used to standardize pay bands and performance expectations. *Related:* Designation, Salary Structure.

**Reporting Manager**
The employee to whom another employee reports; the default first-level approver for many workflows. Circular reporting is not permitted (`BR-EMP-4`). *AKA:* Manager (organizational). *Related:* Manager (role), Team Lead.

**Organizational Hierarchy**
The structure formed by Company → Branch → Department → Designation and reporting lines, used for scoping and approvals.

---

## 3. People & Roles

**Employee** *(module: `EMP`)*
An individual employed by a Company, represented by an authoritative master record spanning personal, employment, statutory, and financial details, with a lifecycle status (active, on-notice, suspended, exited). The central reference entity for most modules. *Related:* Employee Lifecycle, Reporting Manager.

**Employee Lifecycle**
The end-to-end stages of an employee's association with a Company: onboarding → active service → (transfers/changes) → offboarding/exit. *Related:* Onboarding, Offboarding.

**Candidate** *(module: `REC`)*
An external applicant for a job, with tightly scoped access limited to their own application via the candidate portal. May be converted to an Employee on hire. *Related:* Recruitment, Requisition.

**Role (RBAC)**
A named set of permissions (e.g., HR Manager, Employee) assigned to a user to control access. Distinct from Designation (a job title). Defined authoritatively in [`permissions-matrix`](./permissions-matrix.md). *Related:* Permission, Data Scope.

**Super Admin / Company Admin / HR Manager / HR Executive / Manager / Team Lead / Recruiter / Finance**
The platform's stakeholder roles. See [`permissions-matrix`](./permissions-matrix.md) §2 for definitions and data scopes.

**Stakeholder**
Any role or party with an interest in or interaction with the system (internal roles plus Candidate).

---

## 4. Access & Security

**Authentication (`AUTH`)**
Verifying a user's identity to establish an authenticated session. *Related:* Token, MFA, Authorization.

**Authorization / RBAC (`AUTHZ`)**
Determining whether an authenticated user may perform a specific action, based on role-based permissions and data scope. *Related:* Permission, Data Scope, Permissions Matrix.

**Permission**
A grant to perform a category of action on a module: View, Create, Edit, Delete, Approve, Export, or Admin. *Related:* Role, Permissions Matrix.

**Permissions Matrix**
The authoritative mapping of roles to permissions per module. See [`permissions-matrix`](./permissions-matrix.md).

**Data Scope**
The subset of records a role may act upon (self, team, department, company, or platform-wide). Enforced *in addition* to the action permission (`BR-AUTHZ-3`). *Related:* Tenant, Role.

**Deny-by-Default**
The principle that any action not explicitly permitted is denied (`BR-AUTHZ-1`).

**Token (JWT)**
A signed, time-limited credential issued at login and presented on subsequent requests to prove authentication, paired with a revocable refresh mechanism. *Related:* Authentication, SSO.

**MFA (Multi-Factor Authentication)**
An additional verification factor beyond a password, configurable as company policy. *Related:* Authentication.

**SSO (Single Sign-On)**
Authentication delegated to an external identity provider (OAuth2/OIDC), planned as a future integration. *Related:* Authentication.

**Audit Log (`AUDIT`)**
An immutable, append-only record of who did what, to what, when, and with what outcome — for sensitive operations across all modules. *Related:* Auditability, Compliance.

**Auditability**
The property that sensitive actions leave a traceable, tamper-evident record. A non-functional requirement across the system.

---

## 5. Time & Attendance

**Attendance (`ATT`)**
The record of an employee's presence/working time, captured primarily via workplace-network (Wi-Fi/IP) validation and reconciled against shift and holiday rules to derive daily status. *Related:* Shift, Regularization, Network Identifier.

**Network Identifier**
An approved Wi-Fi SSID/BSSID or IP range configured per Branch used to validate that an attendance event originates from an authorized workplace network. *Related:* Attendance, Branch.

**Check-in / Check-out**
The events marking the start/end of an employee's working presence, subject to network validation. *Related:* Attendance.

**Regularization**
A request to correct attendance (e.g., a missed or invalid punch), subject to approval, after which the day's status and payroll inputs are updated. *Related:* Attendance, Approval Workflow.

**Shift (`SHIFT`)**
A defined work pattern (start/end times, breaks, grace periods, overnight handling) assigned to employees, departments, or branches — possibly as a rotating roster. Each employee has exactly one effective shift per date (`BR-SHIFT-1`). *Related:* Roster, Attendance.

**Roster**
A schedule assigning employees to shifts over time, including rotations. *Related:* Shift.

**Grace Period**
A configured tolerance after a shift start within which an employee is not marked late. *Related:* Shift, Attendance.

**Leave (`LEAVE`)**
Authorized time away from work of a defined type (paid, sick, casual, unpaid, etc.), drawn against a balance and subject to approval. *Related:* Leave Balance, Leave Type, Loss of Pay.

**Leave Type**
A category of leave with its own accrual, carry-forward, and pay rules (e.g., paid vs. unpaid). *Related:* Leave, Accrual.

**Leave Balance**
The quantity of a given leave type an employee currently has available. Applied-but-unapproved leave provisionally holds balance to prevent overdraw (`BR-LEAVE-1`). *Related:* Accrual, Carry-Forward.

**Accrual**
The rule-based accumulation of leave balance over time (e.g., monthly). *Related:* Leave Balance, Carry-Forward.

**Carry-Forward**
The policy governing how unused leave balance transfers to a subsequent period. *Related:* Leave Balance, Accrual.

**Holiday (`HOL`)**
A designated non-working day defined in a calendar scoped to company, branch, or region; may be mandatory or optional (floating). Respected by attendance, leave, and payroll. *Related:* Holiday Calendar.

**Holiday Calendar**
A set of holidays applicable to a scope (company/branch/region); region/branch calendars override the company default where defined (`BR-HOL-1`). *Related:* Holiday.

**Loss of Pay (LOP)**
A pay reduction resulting from unpaid absence (e.g., unpaid leave or unauthorized absence) applied during payroll. *Related:* Leave, Payroll.

---

## 6. Compensation & Finance

**Payroll (`PAY`)**
The process of computing employee compensation for a cycle from verified attendance, leave, and configured pay rules, producing payslips and disbursement data. *Related:* Payroll Cycle, Salary Structure, Payslip.

**Payroll Cycle**
The recurring period (e.g., monthly) over which payroll is calculated and run. Attendance and leave for the period are finalized/locked before the run (`BR-PAY-1`). *AKA:* Pay Period, Pay Run (a single execution). *Related:* Payroll, Finalization/Lock.

**Salary Structure**
The configured composition of an employee's pay: earnings, deductions, and statutory components, often tied to grade/designation. *Related:* CTC, Gross Salary, Net Salary.

**CTC (Cost to Company)**
The total annual cost a Company incurs for an employee, including gross salary plus employer-borne contributions and benefits. A budgeting/offer figure, typically greater than take-home pay. *Related:* Gross Salary, Net Salary, Salary Structure.

**Gross Salary**
An employee's total earnings for a period before deductions (basic + allowances + other earnings). *Related:* Net Salary, Deduction, CTC.

**Net Salary**
The amount payable to the employee after all deductions from gross (i.e., take-home pay). *AKA:* Take-Home Pay. *Related:* Gross Salary, Deduction.

**Deduction**
An amount subtracted from gross salary (e.g., statutory contributions, taxes, recoveries) to arrive at net salary. *Related:* Statutory Component, Net Salary.

**Statutory Component**
A pay element mandated by jurisdiction-specific regulation (e.g., social-security/tax contributions), configured per company. *Related:* Deduction, Compliance.

**Payslip**
A per-employee, per-cycle statement itemizing earnings, deductions, and net pay. Generated at payroll finalization and available to the employee. *Related:* Payroll, Document.

**Finalization / Lock**
The point at which a payroll run (and its source attendance/leave for the period) becomes immutable; later corrections occur as audited adjustments in a subsequent cycle (`BR-PAY-2`). *Related:* Payroll Cycle, Adjustment.

**Adjustment**
A correction applied in a later payroll cycle to remedy an error in a finalized run, preserving the immutability of the original. *Related:* Finalization/Lock.

**Disbursement**
The actual payout of net salary (and approved reimbursements) to employees, executed/recorded by Finance from finalized payroll output. *Related:* Payroll, Finance.

**Expense Claim (`EXP`)**
A request by an employee for reimbursement of an incurred business expense, submitted with supporting receipts and routed through approval. *Related:* Reimbursement, Approval Workflow.

**Reimbursement**
The repayment to an employee of an approved expense claim, paid standalone or via payroll. *Related:* Expense Claim, Disbursement.

**Policy Limit**
A configured threshold (e.g., maximum claim amount) that governs validation and approval routing for expenses and similar workflows. *Related:* Expense Claim, Settings.

---

## 7. Talent

**Recruitment (`REC`)**
The end-to-end hiring process from requisition through sourcing, screening, interviews, and offer, including a candidate-facing portal. *AKA:* Applicant Tracking (ATS). *Related:* Requisition, Candidate, Pipeline.

**Requisition**
An approved request to hire for a role, defining the position before it is published to candidates. *Related:* Recruitment, Approval Workflow.

**Pipeline / Pipeline Stage**
The sequence of stages a candidate's application moves through (applied → screened → interview → offer → hired/rejected). *Related:* Candidate, Recruitment.

**Offer**
A formal proposal of employment extended to a selected candidate; acceptance triggers conversion to onboarding. *Related:* Candidate, Onboarding.

**Onboarding** *(workflow)*
The process of bringing a new hire to fully set-up, active employee status (record, account, org placement, policies, documents, assets). *Related:* Employee Lifecycle, Offboarding.

**Offboarding / Exit** *(workflow)*
The process of cleanly exiting an employee — asset recovery, dues settlement, access revocation — ending in status "exited" with records retained. *AKA:* Separation, Resignation (employee-initiated). *Related:* Final Settlement, Notice Period.

**Notice Period**
The time between resignation/termination and the last working day, computed per policy. *Related:* Offboarding.

**Final Settlement**
The computation of an exiting employee's remaining dues and recoveries (a.k.a. full-and-final). *Related:* Offboarding, Payroll.

**Performance Management (`PERF`)**
Structured cycles of goal-setting and review (self, manager, optionally peer/360) producing recorded outcomes. *Related:* Review Cycle, Goal.

**Goal / Objective**
A defined target set for an employee within a performance cycle, against which performance is assessed. *Related:* Performance Management.

**Review Cycle**
A time-boxed performance evaluation period with defined participants and deadlines. *Related:* Performance Management, Goal.

---

## 8. Operations & Communication

**Asset (`ASSET`)**
A company-owned item (e.g., laptop, access card) tracked in inventory and assignable to at most one employee at a time, with an immutable assignment history. *Related:* Asset Assignment, Offboarding.

**Asset Assignment**
The record of an asset issued to an employee and its subsequent return. *Related:* Asset.

**Document (`DOC`)**
A stored file with metadata, ownership, and access control; may require an approval or acknowledgement workflow and may have versioning and expiry. *Related:* Document Approval, Acknowledgement.

**Acknowledgement**
A recorded confirmation (with timestamp and actor) that a user has read/accepted a document (e.g., a policy). *Related:* Document, Compliance.

**Announcement (`ANN`)**
A broadcast message targeted to an audience (company/branch/department/role) across in-app and optional external channels. *Related:* Notification, Targeting.

**Targeting**
The selection of an audience for an announcement or notification, constrained by the author's scope and tenant boundary. *Related:* Announcement, Data Scope.

**Notification (`NOTIF`)**
A channel-agnostic alert to a user about a system event (approval, status change, reminder), delivered in-app and/or via external channels per preference. *Related:* Email Engine, WhatsApp Engine, Announcement.

**Email Engine (`EMAIL`)**
The service that sends reliable, templated, localized transactional emails. *Related:* Notification, Template.

**WhatsApp Engine (`WA`)**
The optional service that delivers consent-based, template-approved WhatsApp messages via an approved provider. *Related:* Notification, Consent.

**Consent**
A recipient's explicit opt-in required before messaging over channels such as WhatsApp (`BR-WA-1`). *Related:* WhatsApp Engine.

**Template**
A reusable, versioned, localizable content definition for emails, WhatsApp messages, or notifications. *Related:* Email Engine, Internationalization.

---

## 9. Intelligence, Analytics & Configuration

**AI HR Assistant (`AI`)**
Scoped, human-supervised AI features (conversational help, drafting, anomaly detection) that reduce HR effort without making autonomous decisions about people (`BR-AI-1`). *Related:* AI Recommendation, Human-in-the-Loop.

**AI Recommendation**
An AI-generated suggestion, draft, or flag (e.g., a drafted job description, a summarized review, an anomaly alert). Always labeled as AI-produced and, for consequential use, requires human review and confirmation (`BR-AI-3`). *Related:* AI HR Assistant, Human-in-the-Loop.

**Human-in-the-Loop**
The principle that a human reviews and authorizes any AI-assisted output that materially affects an individual; AI augments, never autonomously decides. *Related:* AI Recommendation.

**Anomaly Detection**
AI-assisted flagging of unusual patterns (e.g., in attendance or payroll) for human review — never auto-correction. *Related:* AI HR Assistant.

**Report (`RPT`)**
A scoped, exportable dataset or summary across one or more modules, constrained by RBAC and tenant. *Related:* Dashboard, Export.

**Dashboard (`DASH`)**
A role-appropriate, real-time landing view summarizing KPIs and pending actions within the user's data scope. *Related:* Report, KPI.

**KPI (Key Performance Indicator)**
A measured value tracking a success metric (e.g., leave approval turnaround, payroll processing time). *Related:* Success Metric, Dashboard.

**Success Metric**
A measurable outcome used to judge whether a business goal is met (see vision doc §6). *Related:* KPI.

**Settings (`SET`)**
Platform- and company-level configuration (policies, integrations, preferences, branding, locale) consumed by other modules. Company settings cannot weaken platform security baselines (`BR-SET-1`). *Related:* Policy, Multi-Tenancy.

**Policy**
A configured rule set governing a domain (leave, attendance, payroll, expense) that modules enforce. *Related:* Settings, Business Rule.

---

## 10. Process & Governance

**Approval Workflow**
The common pattern by which a request (leave, expense, document, requisition, payroll, regularization) is routed by RBAC scope to one or more approvers, decided, and its effects applied — with the full sequence audited. Multi-level approvals proceed until fully approved. *Related:* Approve (permission), Data Scope, Audit Log.

**Approver**
A user authorized (by role and scope) to decide an item in an approval workflow. *Related:* Approval Workflow.

**Business Rule**
An invariant or policy the system must enforce (IDed as `BR-<MODULE>-n` in the functional requirements). *Related:* Functional Requirement, Policy.

**Functional Requirement**
A testable capability the system must provide (IDed as `FR-<MODULE>-n`). *Related:* User Story, Acceptance Criteria.

**User Story**
A role-centric statement of need ("As a … I want … so that …"), IDed as `US-<MODULE>-n`. *Related:* Acceptance Criteria.

**Acceptance Criteria**
The conditions (Given/When/Then) that prove a requirement or story is satisfied. *Related:* User Story, Functional Requirement.

**Soft Delete**
Marking a record inactive/removed while retaining it for history, audit, or statutory needs, rather than physically deleting it. Mandated for records with financial/historical significance. *Related:* Auditability, Employee, Payroll.

**Internationalization (i18n) / Localization (l10n)**
Designing for multiple languages, locales, currencies, time zones, and jurisdiction-specific rules (i18n), and adapting content/data to a specific locale (l10n). *Related:* Template, Settings.

**Foundation**
The current state of the codebase: a booting application with security scaffold and package structure but no business modules yet. All modules in this volume are target scope built on the Foundation. *Related:* Module.

**Module**
A cohesive functional area of the HRMS (e.g., Payroll, Leave), each specified in the functional requirements with its own prefix. *Related:* Functional Requirement.

---

## 11. Abbreviations

| Abbreviation | Expansion |
|--------------|-----------|
| HRMS | Human Resource Management System |
| RBAC | Role-Based Access Control |
| ATS | Applicant Tracking System |
| CTC | Cost to Company |
| LOP | Loss of Pay |
| JWT | JSON Web Token |
| MFA | Multi-Factor Authentication |
| SSO | Single Sign-On |
| OIDC | OpenID Connect |
| KPI | Key Performance Indicator |
| SLO | Service-Level Objective |
| PII | Personally Identifiable Information |
| i18n / l10n | Internationalization / Localization |
| FR / US / BR / AC | Functional Requirement / User Story / Business Rule / Acceptance Criteria |

Module prefixes (`AUTH`, `AUTHZ`, `COMP`, `BRANCH`, `DEPT`, `DESIG`, `EMP`, `ATT`, `SHIFT`, `LEAVE`, `HOL`, `PAY`, `REC`, `PERF`, `ASSET`, `EXP`, `DOC`, `ANN`, `NOTIF`, `EMAIL`, `WA`, `AI`, `RPT`, `DASH`, `SET`, `AUDIT`) are defined in [`functional-requirements`](./functional-requirements.md) §2.

---

*End of document. Living document; the shared vocabulary for the HRMS. Add new terms here before using them in code or other documents.*
