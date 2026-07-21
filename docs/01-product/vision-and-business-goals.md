# HRMS — Vision & Business Goals

> **Document type:** Software Requirements Specification (SRS) — Product Vision
> **Product:** OGM Human Resource Management System (HRMS)
> **Document ID:** `01-product/vision-and-business-goals`
> **Status:** Draft v1.0 (Foundation)
> **Owner:** Product Management
> **Audience:** Senior Software Engineers, Product Managers, Architects, QA, Security
> **Last updated:** 2026-07-16

---

## Document Control

| Version | Date       | Author  | Change summary                          |
|--------:|------------|---------|------------------------------------------|
| 1.0     | 2026-07-16 | Product | Initial vision & business-goals baseline |

**Related documents (planned):** `02-architecture/*`, `03-security/*`, `04-domain-model/*`, `05-api/*`.
This is Document 01 of the HRMS Engineering Bible and establishes the product intent that all subsequent technical documents must trace back to.

---

## 1. Executive Summary

### 1.1 Purpose of the HRMS

The OGM HRMS is a unified, cloud-ready platform that manages the complete employee lifecycle — from recruitment and onboarding, through attendance, leave, and payroll, to performance and offboarding. It replaces the fragmented mix of spreadsheets, email threads, and disconnected point tools that most small-to-mid-sized organizations rely on today.

The purpose of this document is to define **why** the product exists, **what** business outcomes it must deliver, and **for whom** — creating a single, authoritative reference that engineering and product decisions can be validated against. It is intentionally solution-neutral: it states goals and constraints, not implementation.

### 1.2 Vision

> **To become the single source of truth for every people-related process in an organization — so that HR teams spend their time on people, not paperwork.**

The system should feel less like software to be operated and more like an assistant that removes toil: attendance reconciles itself, payroll runs predictably, approvals move without chasing, and every stakeholder sees exactly what they need and nothing they shouldn't.

### 1.3 Mission

Deliver an HRMS that is:

- **Accurate** — payroll and attendance data that finance and employees can trust without manual re-checking.
- **Automated** — routine HR operations run with minimal human intervention.
- **Accessible** — usable by non-technical HR staff and employees on web and mobile.
- **Extensible** — a multi-tenant foundation that scales from one company to many without redesign.
- **Intelligent** — AI-assisted where it measurably reduces effort or error.

### 1.4 Business Objectives

| # | Objective | Target outcome |
|---|-----------|----------------|
| O1 | Eliminate manual, spreadsheet-driven HR processes | ≥ 70% reduction in manual HR data entry |
| O2 | Make payroll fast, correct, and auditable | Payroll cycle completed in hours, not days; zero silent miscalculations |
| O3 | Automate attendance capture and reconciliation | Near-zero manual attendance correction |
| O4 | Shorten hiring and onboarding cycles | Measurable reduction in time-to-hire and time-to-productive |
| O5 | Improve employee self-service and satisfaction | Employees resolve common requests without HR involvement |
| O6 | Support multiple companies on one platform | Multi-tenant isolation with per-company configuration |
| O7 | Provide decision-grade reporting | Real-time dashboards for HR, finance, and leadership |

---

## 2. Business Goals

Each goal below states the problem, the intended outcome, and how success will be recognized. Detailed metrics are consolidated in [Section 6](#6-success-metrics).

### 2.1 Reduce Manual HR Work
**Problem:** HR teams spend disproportionate time on data entry, document chasing, and reconciliation across disconnected tools.
**Outcome:** Digitize and automate repetitive workflows (records, approvals, document generation) so HR shifts from clerical work to strategic work.
**Recognized by:** Reduction in manual touchpoints per HR transaction; fewer tools in the HR stack.

### 2.2 Automate Attendance
**Problem:** Attendance is captured inconsistently (biometric, manual registers, remote check-ins) and reconciled by hand, causing payroll disputes.
**Outcome:** Capture attendance from multiple sources, apply shift/policy rules automatically, and surface exceptions rather than requiring line-by-line review.
**Recognized by:** Attendance processed automatically for the majority of employees; exceptions flagged, not hunted.

### 2.3 Simplify Payroll
**Problem:** Payroll depends on error-prone spreadsheets combining attendance, leave, tax, and statutory rules.
**Outcome:** A rules-driven payroll engine that consumes verified attendance/leave data and produces accurate, auditable payslips with a clear breakdown.
**Recognized by:** Faster payroll runs, full auditability, and a sharp drop in post-payroll corrections.

### 2.4 Improve Recruitment
**Problem:** Hiring is managed across job boards, inboxes, and spreadsheets, with poor visibility into pipeline health.
**Outcome:** An applicant tracking capability covering requisition, sourcing, screening, interviews, and offer — with a candidate-facing experience.
**Recognized by:** Shorter time-to-hire, higher pipeline visibility, and a smoother candidate journey.

### 2.5 Improve Employee Experience
**Problem:** Employees lack a single place to view records, request leave, access payslips, or update details, and depend on HR for routine tasks.
**Outcome:** A self-service portal that gives employees control over common needs and transparency into their own data.
**Recognized by:** High self-service adoption; fewer routine tickets to HR.

### 2.6 AI-Assisted HR Operations
**Problem:** Judgment-light but time-consuming tasks (drafting job descriptions, answering policy questions, summarizing reviews, flagging anomalies) consume HR bandwidth.
**Outcome:** Targeted AI assistance where it demonstrably reduces effort or error — always with human oversight for decisions affecting people.
**Recognized by:** Time saved on assisted tasks, with no degradation in accuracy, fairness, or compliance.
**Guardrail:** AI augments, never autonomously decides, outcomes that materially affect an individual (hiring, pay, discipline). See [Section 7.6](#76-security) and [Section 8](#8-risks).

### 2.7 Reporting and Analytics
**Problem:** Leadership lacks timely, trustworthy people-data to make decisions.
**Outcome:** Role-appropriate dashboards and exportable reports across headcount, attendance, leave, payroll, and hiring.
**Recognized by:** Decisions made on live data; reduced ad-hoc reporting requests to HR.

### 2.8 Scalability for Multiple Companies
**Problem:** Growth (new subsidiaries, clients, or business units) typically forces re-platforming.
**Outcome:** A multi-tenant architecture where each company has isolated data and independent configuration (policies, pay rules, branding) on shared infrastructure.
**Recognized by:** Onboarding a new company is a configuration exercise, not an engineering project.

---

## 3. Project Scope

Scope is expressed at the product-vision level. Detailed, release-bound scope belongs in per-module specifications. The current codebase is a **foundation only** (no HRMS business modules implemented yet); everything in "In Scope" below is target scope to be delivered incrementally.

### 3.1 In Scope

- **Core HR / Employee Master:** employee records, org structure, departments, roles, documents.
- **Attendance & Time:** multi-source capture, shifts, policy rules, reconciliation, exceptions.
- **Leave Management:** leave types, balances, accrual, approval workflows, calendars.
- **Payroll:** rules-driven calculation, statutory components, payslips, disbursement records.
- **Recruitment (ATS):** requisitions, candidate pipeline, interviews, offers, candidate portal.
- **Onboarding / Offboarding:** structured joining and exit workflows.
- **Employee Self-Service:** profile, requests, payslips, documents.
- **Role-Based Access Control:** fine-grained permissions across all stakeholder roles.
- **Multi-Tenancy:** per-company isolation and configuration.
- **Reporting & Analytics:** dashboards and exports across modules.
- **AI-Assisted Features:** scoped assistance with human oversight (see [2.6](#26-ai-assisted-hr-operations)).
- **Notifications:** email and in-app notifications for approvals and events.
- **Audit & Compliance:** traceable change history on sensitive data.

### 3.2 Out of Scope (for the foreseeable roadmap)

- **Full-suite accounting / ERP / general ledger** (integration, not replacement).
- **Learning Management System (LMS)** as a full product.
- **Dedicated project management / task tracking** beyond HR workflows.
- **CRM / sales tooling.**
- **Hardware provisioning** (biometric devices, kiosks) — the system integrates with device data feeds but does not manufacture or manage hardware.
- **Country-specific statutory engines** beyond the initially targeted jurisdictions (added deliberately, not assumed).
- **Autonomous AI decision-making** on hiring, compensation, or disciplinary outcomes.

Items may move into scope through the formal roadmap process; nothing here is a permanent exclusion except autonomous AI decisioning.

---

## 4. Stakeholders

Roles below define **responsibilities and intent**. The concrete permission model (RBAC) is specified in the security documentation; permission summaries here are indicative, not authoritative.

| Role | Primary responsibility | Scope of data |
|------|------------------------|---------------|
| **Super Admin** | Platform-level administration across all tenants; provisions companies, manages global configuration, oversees platform health and security. | All tenants (platform-wide) |
| **Company Owner** | Owns a single tenant (company); highest authority within that company; configures company-wide policy and delegates administration. | Own company (full) |
| **HR Manager** | Owns HR operations for a company; approves policy-level actions, oversees payroll/attendance/leave, manages HR staff. | Own company (HR domains) |
| **HR Executive** | Executes day-to-day HR tasks: employee records, onboarding, attendance/leave processing, document handling. | Own company (operational) |
| **Manager** | Leads a department/function; approves leave and attendance exceptions for reports; reviews team performance. | Own reporting line |
| **Team Lead** | Leads a team within a department; first-line approver and reviewer for direct reports. | Own team |
| **Employee** | End user; maintains own profile, submits requests, accesses own records and payslips. | Own records only |
| **Recruiter** | Manages hiring pipelines: requisitions, sourcing, screening, interview coordination, offers. | Recruitment domain |
| **Candidate** | External applicant; applies to roles, tracks application status, submits documents. | Own application only |
| **Finance Team** | Consumes verified payroll output; manages disbursement, statutory remittance, and financial reporting. | Payroll/finance domain |

### 4.1 Responsibility Detail

- **Super Admin** — The only role that operates *above* a single company. Creates and suspends tenants, sets platform-wide security baselines, monitors system health, and manages the subscription/plan boundaries. Deliberately isolated from day-to-day company HR data except where support requires it (and such access must be audited).
- **Company Owner** — Ultimate accountability within one tenant. Configures the company profile, org structure, and top-level policies; assigns HR Managers and administrators. Can view everything in their company but typically delegates operations.
- **HR Manager** — Accountable for correctness and compliance of HR operations. Approves exceptions, signs off payroll runs, defines leave and attendance policies, and supervises HR Executives.
- **HR Executive** — The operational engine of HR. Handles record maintenance, onboarding/offboarding execution, leave and attendance processing, and document generation. Works within policies set by the HR Manager.
- **Manager** — Accountable for their function's people operations: approves leave/attendance for reports, participates in performance cycles, and has visibility into their reporting line.
- **Team Lead** — A lighter-weight manager scope; first approver for a small team, escalating to the Manager where policy requires.
- **Employee** — The largest user population. Interacts primarily through self-service and must never see data beyond their own without explicit delegation.
- **Recruiter** — Owns the hiring funnel end-to-end and is the primary internal counterpart to candidates.
- **Candidate** — External, untrusted-by-default user with tightly scoped access to their own application only.
- **Finance Team** — Downstream consumer of payroll. Responsible for actual disbursement and statutory obligations; requires read access to finalized payroll and the ability to record disbursement outcomes.

---

## 5. User Personas

Personas are representative, not exhaustive. Each includes goals, a typical workflow, pain points the HRMS must relieve, and indicative permissions.

### 5.1 Priya — HR Manager
- **Context:** Runs HR for a 300-person company; accountable for compliance and payroll accuracy.
- **Goals:** Error-free payroll, policy compliance, visibility into HR operations, less firefighting.
- **Daily workflow:** Reviews pending approvals and exceptions → checks attendance/leave anomalies → oversees onboarding in progress → reviews payroll readiness near cycle-end → pulls reports for leadership.
- **Pain points:** Reconciling attendance against leave; chasing approvals; last-minute payroll corrections; fragmented reporting.
- **Permissions (indicative):** Full HR domain for her company; policy configuration; payroll sign-off; manage HR Executives.

### 5.2 Rahul — HR Executive
- **Context:** Processes the operational HR workload day to day.
- **Goals:** Clear task queues, fast data entry, few back-and-forths, confidence that records are correct.
- **Daily workflow:** Onboards new joiners → updates employee records → processes leave/attendance corrections → prepares documents → responds to employee queries.
- **Pain points:** Repetitive manual entry; switching between tools; unclear task ownership; document generation by hand.
- **Permissions (indicative):** Operational read/write on employee, attendance, and leave data; no policy or payroll sign-off authority.

### 5.3 Anita — Employee
- **Context:** A software engineer who interacts with HR only occasionally.
- **Goals:** Apply for leave quickly, see leave balance, download payslips, update personal details — without emailing HR.
- **Daily workflow:** Checks in/out (or is auto-captured) → occasionally requests leave → periodically downloads payslip → updates details when life changes.
- **Pain points:** Not knowing leave balance; opaque approval status; hard-to-find payslips; HR dependency for trivial tasks.
- **Permissions (indicative):** Read/write on **own** records only; submit requests; no visibility into others.

### 5.4 Vikram — Department Manager
- **Context:** Leads a 25-person function; approves for his reporting line.
- **Goals:** Fast approvals, clear view of team availability, minimal admin overhead.
- **Daily workflow:** Reviews and approves leave/attendance exceptions → checks team calendar before planning → participates in performance cycles.
- **Pain points:** Approval requests scattered across email; no consolidated team availability view; context-switching.
- **Permissions (indicative):** Approve/review for direct and indirect reports; read team data; no company-wide access.

### 5.5 Sara — Recruiter
- **Context:** Manages 15–20 open requisitions concurrently.
- **Goals:** Keep pipelines moving, coordinate interviews efficiently, deliver a good candidate experience, reduce time-to-hire.
- **Daily workflow:** Reviews new applicants → screens and shortlists → schedules interviews → collects feedback → progresses or rejects → extends offers.
- **Pain points:** Manual interview scheduling; scattered candidate feedback; poor pipeline visibility; slow candidate communication.
- **Permissions (indicative):** Full recruitment domain; candidate data access; no access to internal payroll or unrelated employee data.

### 5.6 John — Candidate (External)
- **Context:** Applying for a role; interacts with the company only through the candidate portal.
- **Goals:** Apply easily, know where his application stands, submit documents securely.
- **Daily workflow:** Discovers role → applies → tracks status → responds to interview invites → submits requested documents.
- **Pain points:** Application black holes; no status visibility; clunky document submission.
- **Permissions (indicative):** Access to **own application only**; untrusted-by-default; strict data-minimization.

### 5.7 Meera — Finance Team Member
- **Context:** Responsible for salary disbursement and statutory remittance.
- **Goals:** Receive accurate, finalized payroll; disburse on time; keep clean audit and compliance records.
- **Daily workflow:** Reviews finalized payroll → validates disbursement data → executes payments → records outcomes → prepares statutory/financial reports.
- **Pain points:** Late or inaccurate payroll hand-offs; manual reconciliation; audit-trail gaps.
- **Permissions (indicative):** Read finalized payroll; record disbursement outcomes; finance reporting; no ability to alter attendance/leave inputs.

### 5.8 Deepak — Super Admin (Platform)
- **Context:** Operates the platform for multiple client companies.
- **Goals:** Reliable, secure multi-tenant operation; fast company onboarding; clear platform health.
- **Daily workflow:** Provisions/suspends companies → monitors system health and security → manages global configuration → supports tenant administrators.
- **Pain points:** Tenant isolation risks; noisy operational signals; manual provisioning.
- **Permissions (indicative):** Platform-wide administration; audited access to tenant data only when support requires.

---

## 6. Success Metrics

Metrics are the objective evidence that business goals are met. Baselines are captured at rollout; targets are directional for v1 and refined per module spec. All targets are **measured, not assumed.**

| # | Metric | Definition | Directional target |
|---|--------|------------|--------------------|
| M1 | **Attendance processing time** | Effort/time to reconcile a pay-period's attendance | Reduce by ≥ 80% vs. manual baseline |
| M2 | **Payroll processing time** | Elapsed time to complete a payroll run | Hours, not days; ≥ 75% reduction |
| M3 | **Employee onboarding time** | Time from offer-accepted to fully set-up employee | Reduce by ≥ 50% |
| M4 | **Leave approval turnaround** | Median time from request to decision | < 24 business hours |
| M5 | **HR productivity** | HR transactions handled per HR FTE | Measurable increase; fewer manual touchpoints |
| M6 | **User satisfaction** | Satisfaction score for HR/employee/candidate users | Sustained high score (e.g., CSAT/NPS targets per release) |
| M7 | **Self-service adoption** | % of routine requests completed without HR | Majority of routine requests self-served |
| M8 | **Payroll accuracy** | % of payroll runs with zero post-run corrections | Trend toward ~100% |
| M9 | **Time-to-hire** | Requisition-open to offer-accepted | Measurable reduction vs. baseline |

**Measurement principle:** Each metric must have an owner, an instrumented data source, and a baseline before a target is declared "met."

---

## 7. Non-Functional Goals

Non-functional requirements (NFRs) are first-class. Concrete, testable thresholds are defined in the architecture and security specs; the goals below set intent and priority.

### 7.1 Performance
Interactive operations should feel instant for typical loads; batch operations (payroll, reports) should complete within predictable, published windows. Performance budgets are defined per critical path and enforced in testing.

### 7.2 Scalability
The system must scale **horizontally** and support **multi-tenancy** without redesign — growth in companies, employees, or transaction volume is absorbed by configuration and capacity, not re-architecture.

### 7.3 Availability
Business-critical paths (self-service, attendance capture, approvals) target high availability with a published SLO. Planned maintenance must not disrupt time-sensitive operations (e.g., payroll cut-off).

### 7.4 Reliability
Data integrity is non-negotiable, especially for payroll and attendance. Operations affecting money or compliance must be transactional, idempotent where retried, and recoverable. No silent data loss or silent miscalculation.

### 7.5 Maintainability
A modular, layered, well-documented codebase (this Engineering Bible is part of that commitment) so new modules and engineers onboard quickly. Consistency, testability, and clear boundaries are prioritized over cleverness.

### 7.6 Security
Security and privacy are foundational, not features. Requirements include strong authentication and authorization (RBAC), strict tenant isolation, least-privilege access, encryption of sensitive data in transit and at rest, full auditability of sensitive changes, and privacy-by-design for personal data. AI features operate under human oversight and data-minimization. Detailed controls live in the security documentation.

### 7.7 Accessibility
Interfaces target recognized accessibility standards (e.g., WCAG AA) so employees and candidates with disabilities can use core flows. Accessibility is validated, not assumed.

### 7.8 Internationalization
The platform is designed for i18n/l10n from the outset: multiple languages, locale-aware dates/numbers/currencies, configurable time zones, and jurisdiction-specific rules — enabling expansion without core rework.

---

## 8. Risks

| # | Risk | Impact | Likelihood | Mitigation direction |
|---|------|--------|-----------|----------------------|
| R1 | **Payroll/statutory inaccuracy** | High | Medium | Rules-driven engine, strong test coverage, auditability, phased jurisdiction rollout |
| R2 | **Multi-tenant data leakage** | High | Medium | Enforced tenant isolation, least-privilege RBAC, security review, penetration testing |
| R3 | **Scope creep** (feature suite expands uncontrolled) | High | High | Strict scope governance ([Section 3](#3-project-scope)); roadmap discipline; module-bound specs |
| R4 | **Compliance drift** across jurisdictions/regulations | High | Medium | Configurable rules, legal review, deliberate jurisdiction expansion |
| R5 | **AI misuse / bias / over-reliance** | High | Medium | Human-in-the-loop, no autonomous people-decisions, bias monitoring, transparency |
| R6 | **Adoption failure** (users revert to spreadsheets) | High | Medium | UX focus, self-service value, change management, measurable satisfaction targets |
| R7 | **Integration fragility** (biometric feeds, finance, email) | Medium | Medium | Well-defined integration contracts, graceful degradation, monitoring |
| R8 | **Data migration errors** from legacy systems | Medium | Medium | Validated migration tooling, dry-runs, reconciliation reports |
| R9 | **Availability during critical windows** (payroll cut-off) | High | Low | HA design, maintenance scheduling around cut-offs, tested recovery |
| R10 | **Security breach of sensitive PII/financial data** | High | Low–Medium | Defense-in-depth, encryption, auditing, incident response plan |

Risks are reviewed each release; owners and concrete controls are tracked in the risk register.

---

## 9. Assumptions

1. Companies onboarded onto the platform have a defined organizational structure (departments, roles, reporting lines) that can be modeled.
2. Attendance data sources (biometric devices, apps, manual entry) can expose data through integrable feeds or interfaces.
3. Statutory and tax rules for a target jurisdiction are obtainable, encodable, and maintainable as they change.
4. Users have reasonable internet access and modern browsers/devices for a web-first (and later mobile) experience.
5. Email (and later additional channels) is available for notifications.
6. Each company's data can be logically isolated within a shared multi-tenant platform.
7. The organization commits to change management so users adopt the system rather than reverting to legacy tools.
8. AI-assisted features will always operate with human oversight for decisions affecting individuals.
9. The current codebase is a foundation; business modules will be delivered incrementally per their own specifications.
10. Reference/statutory data and initial employee data can be migrated or entered during onboarding.

Assumptions are validated during discovery for each module; a broken assumption is escalated as a risk.

---

## 10. Future Vision

The long-term ambition extends beyond digitizing HR into making HR **proactive and intelligent**, while never removing human judgment from decisions about people.

- **Predictive people analytics** — attrition risk, workforce planning, and capacity forecasting from trusted historical data.
- **Deeper AI assistance** — conversational HR help, automated drafting (JDs, letters, policy answers), anomaly detection in attendance/payroll — always human-supervised.
- **Mobile-first everywhere** — full parity for employees, managers, and candidates on mobile.
- **Marketplace & integrations** — a well-documented API and integration ecosystem (finance/ERP, identity providers, communication tools, job boards).
- **Global expansion** — broader jurisdiction coverage for payroll and compliance via the i18n and configurable-rules foundation.
- **Employee well-being & engagement** — surveys, pulse feedback, recognition, and engagement signals.
- **Self-optimizing workflows** — the system suggests policy and process improvements from observed operational data.
- **Platform scale** — from single companies to large multi-entity groups and, potentially, an offering for HR service providers managing many clients.

Every future capability inherits the same non-negotiables: accuracy, security, privacy, human oversight of people-decisions, and multi-tenant scalability.

---

*End of document. This is a living document; changes follow the document-control process in the header and must remain consistent with subsequent Engineering Bible documents.*
