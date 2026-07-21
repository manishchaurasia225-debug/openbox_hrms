# HRMS — RBAC Permissions Matrix

> **Document type:** Security / Access-Control Specification
> **Product:** OGM Human Resource Management System (HRMS)
> **Document ID:** `01-product/permissions-matrix`
> **Status:** Draft v1.0 (Foundation)
> **Owner:** Security & Product
> **Audience:** Senior Software Engineers, Security, Architects, QA
> **Last updated:** 2026-07-16
> **Traces to:** [`functional-requirements`](./functional-requirements.md) (`AUTHZ`), [`vision-and-business-goals`](./vision-and-business-goals.md)

---

## Document Control

| Version | Date       | Author           | Change summary                 |
|--------:|------------|------------------|---------------------------------|
| 1.0     | 2026-07-16 | Security/Product | Initial RBAC matrix (26 modules) |

---

## 1. Purpose

This document is the **authoritative** role-based access-control (RBAC) matrix for the HRMS. It defines, for every module, which permission each role holds. All authorization decisions (`AUTHZ`) MUST conform to this matrix. Where earlier documents describe permissions as "indicative," this document governs.

---

## 2. Roles

| Role | Description | Default data scope |
|------|-------------|--------------------|
| **Super Admin** | Platform operator across all tenants | Platform-wide (all companies) |
| **Company Admin** | Owner/administrator of one company (a.k.a. Company Owner) | Entire own company |
| **HR Manager** | Head of HR operations for a company | Company-wide HR domains |
| **HR Executive** | Day-to-day HR operator | Operational, company scope (no policy/sign-off) |
| **Manager** | Department/function leader | Own reporting line |
| **Team Lead** | Team leader within a department | Own team |
| **Employee** | Standard end user | Self only |
| **Recruiter** | Hiring pipeline owner | Recruitment domain (company) |
| **Finance** | Payroll/finance/expense processor | Finance domains (company) |

> **Candidate** (external applicant) is intentionally excluded from this internal matrix; candidate access is limited to the candidate portal (own application only) and is specified in the Recruitment module and security docs, not in the internal RBAC grid.

## 3. Permission Types

| Permission | Meaning |
|------------|---------|
| **View** | Read/list records within the role's data scope |
| **Create** | Create new records |
| **Edit** | Modify existing records |
| **Delete** | Remove records (soft-delete where mandated) |
| **Approve** | Authorize/decide items in an approval workflow |
| **Export** | Export/download data (e.g., CSV/PDF) |
| **Admin** | Configure the module (policies, settings, structural changes) |

## 4. Legend & Conventions

- **✔** = permitted · **—** = not permitted.
- All grants are **implicitly scoped** by the role's data scope in §2 (e.g., a Manager's "View" means *their reporting line*, an Employee's means *their own record*). Scope suffixes are added only where clarification helps: `(self)`, `(team)`, `(own)`, `(all-tenants)`.
- **Deny-by-default** (`BR-AUTHZ-1`): any cell not marked ✔ is denied.
- **Tenant isolation** (`BR-AUTHZ-2`) overrides every grant: only Super Admin may cross tenants, and such access is audited.
- Every permitted action is subject to audit (`AUDIT`).
- These grants are the **baseline**; companies MAY define custom roles/overrides within platform limits (`FR-AUTHZ-4`), but MUST NOT exceed platform security baselines (`BR-SET-1`).

---

## 5. Platform & Security Modules

### 5.1 Authentication (`AUTH`)
> Applies to account/session administration, not a user's own login (every authenticated user can manage their own session/password).

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (all-tenants) | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Manager | ✔ | ✔ | ✔ | — | — | — | — |
| HR Executive | ✔ | ✔ | ✔ | — | — | — | — |
| Manager | — | — | — | — | — | — | — |
| Team Lead | — | — | — | — | — | — | — |
| Employee | ✔ (self) | — | ✔ (self) | — | — | — | — |
| Recruiter | — | — | — | — | — | — | — |
| Finance | — | — | — | — | — | — | — |

### 5.2 Authorization / RBAC (`AUTHZ`)
> Role assignment and permission configuration.

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (all-tenants) | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Manager | ✔ | — | ✔ | — | — | — | — |
| HR Executive | ✔ | — | — | — | — | — | — |
| Manager | — | — | — | — | — | — | — |
| Team Lead | — | — | — | — | — | — | — |
| Employee | — | — | — | — | — | — | — |
| Recruiter | — | — | — | — | — | — | — |
| Finance | — | — | — | — | — | — | — |

### 5.3 Settings (`SET`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (platform) | ✔ | ✔ | ✔ | — | ✔ | ✔ (platform) |
| Company Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ (company) |
| HR Manager | ✔ | ✔ | ✔ | — | — | — | ✔ (HR policies) |
| HR Executive | ✔ | — | — | — | — | — | — |
| Manager | ✔ (own scope) | — | — | — | — | — | — |
| Team Lead | — | — | — | — | — | — | — |
| Employee | ✔ (own prefs) | — | ✔ (own prefs) | — | — | — | — |
| Recruiter | ✔ (rec settings) | — | ✔ (rec settings) | — | — | — | — |
| Finance | ✔ (finance settings) | — | ✔ (finance settings) | — | — | — | — |

### 5.4 Audit Logs (`AUDIT`)
> Audit is read-only by design (append-only); no role may Create/Edit/Delete audit records through the application.

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (all-tenants) | — | — | — | — | ✔ | ✔ (retention) |
| Company Admin | ✔ (company) | — | — | — | — | ✔ | — |
| HR Manager | ✔ (HR scope) | — | — | — | — | ✔ | — |
| HR Executive | — | — | — | — | — | — | — |
| Manager | — | — | — | — | — | — | — |
| Team Lead | — | — | — | — | — | — | — |
| Employee | — | — | — | — | — | — | — |
| Recruiter | — | — | — | — | — | — | — |
| Finance | ✔ (finance scope) | — | — | — | — | ✔ | — |

---

## 6. Organization Modules

### 6.1 Company Management (`COMP`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (all-tenants) | ✔ | ✔ | ✔ | ✔ (suspend/activate) | ✔ | ✔ |
| Company Admin | ✔ (own) | — | ✔ (own) | — | — | ✔ | ✔ (own) |
| HR Manager | ✔ (own) | — | — | — | — | — | — |
| HR Executive | ✔ (own) | — | — | — | — | — | — |
| Manager | ✔ (own) | — | — | — | — | — | — |
| Team Lead | ✔ (own) | — | — | — | — | — | — |
| Employee | ✔ (own basic) | — | — | — | — | — | — |
| Recruiter | ✔ (own) | — | — | — | — | — | — |
| Finance | ✔ (own) | — | — | — | — | — | — |

### 6.2 Branch Management (`BRANCH`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Manager | ✔ | ✔ | ✔ | — | — | ✔ | — |
| HR Executive | ✔ | — | ✔ | — | — | — | — |
| Manager | ✔ (own) | — | — | — | — | — | — |
| Team Lead | ✔ (own) | — | — | — | — | — | — |
| Employee | ✔ (own) | — | — | — | — | — | — |
| Recruiter | ✔ | — | — | — | — | — | — |
| Finance | ✔ | — | — | — | — | — | — |

### 6.3 Department Management (`DEPT`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Manager | ✔ | ✔ | ✔ | — | — | ✔ | — |
| HR Executive | ✔ | ✔ | ✔ | — | — | — | — |
| Manager | ✔ (own) | — | — | — | — | ✔ (own) | — |
| Team Lead | ✔ (own) | — | — | — | — | — | — |
| Employee | ✔ (own) | — | — | — | — | — | — |
| Recruiter | ✔ | — | — | — | — | — | — |
| Finance | ✔ | — | — | — | — | — | — |

### 6.4 Designation Management (`DESIG`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Manager | ✔ | ✔ | ✔ | — | — | ✔ | — |
| HR Executive | ✔ | ✔ | ✔ | — | — | — | — |
| Manager | ✔ | — | — | — | — | — | — |
| Team Lead | ✔ | — | — | — | — | — | — |
| Employee | ✔ | — | — | — | — | — | — |
| Recruiter | ✔ | — | — | — | — | — | — |
| Finance | ✔ | — | — | — | — | — | — |

---

## 7. Core HR

### 7.1 Employee Management (`EMP`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (all-tenants) | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ (company) | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| HR Manager | ✔ (company) | ✔ | ✔ | ✔ (soft) | ✔ | ✔ | ✔ |
| HR Executive | ✔ (company) | ✔ | ✔ | — | — | ✔ | — |
| Manager | ✔ (team) | — | — | — | ✔ (profile changes of reports) | ✔ (team) | — |
| Team Lead | ✔ (team) | — | — | — | — | — | — |
| Employee | ✔ (self) | — | ✔ (self, limited) | — | — | — | — |
| Recruiter | ✔ (limited, hiring) | — | — | — | — | — | — |
| Finance | ✔ (payroll-relevant) | — | — | — | — | ✔ | — |

### 7.2 Document Management (`DOC`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| Company Admin | ✔ (company) | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| HR Manager | ✔ (company) | ✔ | ✔ | ✔ (soft) | ✔ | ✔ | ✔ |
| HR Executive | ✔ (company) | ✔ | ✔ | — | — | ✔ | — |
| Manager | ✔ (team) | ✔ | — | — | ✔ (team docs) | — | — |
| Team Lead | ✔ (team) | — | — | — | — | — | — |
| Employee | ✔ (own) | ✔ (own) | ✔ (own, pre-approval) | — | — | ✔ (own) | — |
| Recruiter | ✔ (candidate docs) | ✔ | ✔ | — | — | — | — |
| Finance | ✔ (finance docs) | ✔ | — | — | ✔ (finance) | ✔ | — |

---

## 8. Time & Attendance

### 8.1 Attendance (`ATT`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | — | ✔ | ✔ | ✔ |
| Company Admin | ✔ (company) | — | ✔ | — | ✔ | ✔ | ✔ |
| HR Manager | ✔ (company) | ✔ (corrections) | ✔ | — | ✔ (regularization) | ✔ | ✔ (policy) |
| HR Executive | ✔ (company) | ✔ (corrections) | ✔ | — | ✔ (regularization) | ✔ | — |
| Manager | ✔ (team) | — | — | — | ✔ (team regularization) | ✔ (team) | — |
| Team Lead | ✔ (team) | — | — | — | ✔ (team, 1st level) | — | — |
| Employee | ✔ (self) | ✔ (own check-in / regularization request) | — | — | — | — | — |
| Recruiter | — | — | — | — | — | — | — |
| Finance | ✔ (payroll period) | — | — | — | — | ✔ | — |

### 8.2 Shift Management (`SHIFT`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Manager | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Executive | ✔ | ✔ | ✔ | — | — | — | — |
| Manager | ✔ (team) | — | ✔ (team roster) | — | ✔ (roster) | ✔ (team) | — |
| Team Lead | ✔ (team) | — | ✔ (team roster) | — | — | — | — |
| Employee | ✔ (self) | — | — | — | — | — | — |
| Recruiter | — | — | — | — | — | — | — |
| Finance | ✔ | — | — | — | — | — | — |

### 8.3 Leave Management (`LEAVE`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| Company Admin | ✔ (company) | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| HR Manager | ✔ (company) | ✔ | ✔ | ✔ (soft) | ✔ | ✔ | ✔ (policy) |
| HR Executive | ✔ (company) | ✔ | ✔ | — | ✔ (per policy) | ✔ | — |
| Manager | ✔ (team) | ✔ (on behalf, per policy) | — | — | ✔ (team) | ✔ (team) | — |
| Team Lead | ✔ (team) | — | — | — | ✔ (team, 1st level) | — | — |
| Employee | ✔ (self) | ✔ (own request) | ✔ (own, pre-approval) | ✔ (cancel own) | — | — | — |
| Recruiter | — | — | — | — | — | — | — |
| Finance | ✔ (payroll impact) | — | — | — | — | ✔ | — |

### 8.4 Holiday Management (`HOL`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Manager | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Executive | ✔ | ✔ | ✔ | — | — | — | — |
| Manager | ✔ | — | — | — | — | — | — |
| Team Lead | ✔ | — | — | — | — | — | — |
| Employee | ✔ | — | — | — | — | — | — |
| Recruiter | ✔ | — | — | — | — | — | — |
| Finance | ✔ | — | — | — | — | — | — |

---

## 9. Compensation & Finance

### 9.1 Payroll (`PAY`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | — | — | — | — | ✔ | ✔ (config) |
| Company Admin | ✔ (company) | ✔ (run) | ✔ (pre-final) | — | ✔ (finalize) | ✔ | ✔ |
| HR Manager | ✔ (company) | ✔ (run) | ✔ (pre-final) | — | ✔ (finalize) | ✔ | ✔ (structures) |
| HR Executive | ✔ (company) | ✔ (prepare) | ✔ (pre-final) | — | — | ✔ | — |
| Manager | — | — | — | — | — | — | — |
| Team Lead | — | — | — | — | — | — | — |
| Employee | ✔ (own payslip) | — | — | — | — | ✔ (own payslip) | — |
| Recruiter | — | — | — | — | — | — | — |
| Finance | ✔ (finalized) | — | — | — | ✔ (disbursement) | ✔ | — |

### 9.2 Expense & Reimbursement (`EXP`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | — | — | — | — | ✔ | ✔ (config) |
| Company Admin | ✔ (company) | — | — | — | ✔ | ✔ | ✔ |
| HR Manager | ✔ (company) | — | — | — | ✔ | ✔ | ✔ (policy) |
| HR Executive | ✔ (company) | — | — | — | — | ✔ | — |
| Manager | ✔ (team) | — | — | — | ✔ (team, per limit) | ✔ (team) | — |
| Team Lead | ✔ (team) | — | — | — | ✔ (team, 1st level) | — | — |
| Employee | ✔ (own) | ✔ (own claim) | ✔ (own, pre-approval) | ✔ (withdraw own) | — | — | — |
| Recruiter | ✔ (own) | ✔ (own) | ✔ (own) | ✔ (own) | — | — | — |
| Finance | ✔ (company) | — | — | — | ✔ (final/payout) | ✔ | ✔ (reimbursement config) |

---

## 10. Talent

### 10.1 Recruitment (`REC`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | — | — | — | — | ✔ | ✔ (config) |
| Company Admin | ✔ (company) | ✔ | ✔ | ✔ | ✔ (requisition) | ✔ | ✔ |
| HR Manager | ✔ (company) | ✔ | ✔ | ✔ (soft) | ✔ (requisition/offer) | ✔ | ✔ |
| HR Executive | ✔ (company) | ✔ | ✔ | — | — | ✔ | — |
| Manager | ✔ (own reqs) | ✔ (raise req) | ✔ (own req) | — | ✔ (interview feedback/decision) | — | — |
| Team Lead | ✔ (own reqs) | — | — | — | ✔ (interview feedback) | — | — |
| Employee | — | — | — | — | — | — | — |
| Recruiter | ✔ (assigned) | ✔ | ✔ | ✔ (candidates) | ✔ (stage moves) | ✔ | — |
| Finance | — | — | — | — | — | — | — |

### 10.2 Performance Management (`PERF`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | — | — | — | — | ✔ | ✔ (config) |
| Company Admin | ✔ (company) | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| HR Manager | ✔ (company) | ✔ (cycles) | ✔ | ✔ (soft) | ✔ | ✔ | ✔ |
| HR Executive | ✔ (company) | ✔ | ✔ | — | — | ✔ | — |
| Manager | ✔ (team) | ✔ (reviews of reports) | ✔ (reviews) | — | ✔ (finalize reviews) | ✔ (team) | — |
| Team Lead | ✔ (team) | ✔ (feedback) | ✔ (feedback) | — | — | — | — |
| Employee | ✔ (own) | ✔ (self-review/goals) | ✔ (own, pre-final) | — | — | — | — |
| Recruiter | — | — | — | — | — | — | — |
| Finance | — | — | — | — | — | — | — |

---

## 11. Operations

### 11.1 Asset Management (`ASSET`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ (company) | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| HR Manager | ✔ (company) | ✔ | ✔ | ✔ (soft) | ✔ (assign/return) | ✔ | ✔ |
| HR Executive | ✔ (company) | ✔ | ✔ | — | ✔ (assign/return) | ✔ | — |
| Manager | ✔ (team) | — | — | — | — | ✔ (team) | — |
| Team Lead | ✔ (team) | — | — | — | — | — | — |
| Employee | ✔ (own assigned) | — | — | — | — | — | — |
| Recruiter | — | — | — | — | — | — | — |
| Finance | ✔ (valuation) | — | — | — | — | ✔ | — |

---

## 12. Communication

### 12.1 Announcements (`ANN`)

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ (platform) | ✔ | ✔ | — | — | ✔ |
| Company Admin | ✔ (company) | ✔ (company) | ✔ | ✔ | ✔ | — | ✔ |
| HR Manager | ✔ (company) | ✔ (company) | ✔ | ✔ | ✔ | — | — |
| HR Executive | ✔ (company) | ✔ (per scope) | ✔ (own) | — | — | — | — |
| Manager | ✔ (scope) | ✔ (team/dept) | ✔ (own) | ✔ (own) | — | — | — |
| Team Lead | ✔ (scope) | ✔ (team) | ✔ (own) | — | — | — | — |
| Employee | ✔ (targeted) | — | — | — | — | — | — |
| Recruiter | ✔ (scope) | ✔ (rec-related) | ✔ (own) | — | — | — | — |
| Finance | ✔ (scope) | ✔ (finance-related) | ✔ (own) | — | — | — | — |

### 12.2 Notifications (`NOTIF`)
> User-facing notifications are personal; "Admin" governs company notification configuration/templates.

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ | ✔ (system) | ✔ | ✔ | — | — | ✔ (platform) |
| Company Admin | ✔ (own) | — | ✔ (own prefs) | ✔ (own) | — | — | ✔ (company config) |
| HR Manager | ✔ (own) | — | ✔ (own prefs) | ✔ (own) | — | — | ✔ (HR templates) |
| HR Executive | ✔ (own) | — | ✔ (own prefs) | ✔ (own) | — | — | — |
| Manager | ✔ (own) | — | ✔ (own prefs) | ✔ (own) | — | — | — |
| Team Lead | ✔ (own) | — | ✔ (own prefs) | ✔ (own) | — | — | — |
| Employee | ✔ (own) | — | ✔ (own prefs) | ✔ (own) | — | — | — |
| Recruiter | ✔ (own) | — | ✔ (own prefs) | ✔ (own) | — | — | — |
| Finance | ✔ (own) | — | ✔ (own prefs) | ✔ (own) | — | — | — |

### 12.3 Email Engine (`EMAIL`)
> Infrastructure module; configuration/templates are administrative. No end-user record-level CRUD.

| Role | View (logs) | Create (template) | Edit | Delete | Approve | Export | Admin |
|------|:-----------:|:-----------------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (platform) | ✔ | ✔ | ✔ | — | ✔ | ✔ (platform) |
| Company Admin | ✔ (company) | ✔ | ✔ | ✔ | — | ✔ | ✔ (company) |
| HR Manager | ✔ (company) | ✔ | ✔ | — | — | — | — |
| HR Executive | — | — | — | — | — | — | — |
| Manager | — | — | — | — | — | — | — |
| Team Lead | — | — | — | — | — | — | — |
| Employee | — | — | — | — | — | — | — |
| Recruiter | ✔ (rec templates) | ✔ (rec) | ✔ (rec) | — | — | — | — |
| Finance | — | — | — | — | — | — | — |

### 12.4 WhatsApp Engine (`WA`)

| Role | View (logs) | Create (template) | Edit | Delete | Approve | Export | Admin |
|------|:-----------:|:-----------------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (platform) | ✔ | ✔ | ✔ | — | ✔ | ✔ (platform) |
| Company Admin | ✔ (company) | ✔ | ✔ | ✔ | — | ✔ | ✔ (company/provider) |
| HR Manager | ✔ (company) | ✔ | ✔ | — | — | — | — |
| HR Executive | — | — | — | — | — | — | — |
| Manager | — | — | — | — | — | — | — |
| Team Lead | — | — | — | — | — | — | — |
| Employee | ✔ (own consent) | — | ✔ (own consent) | — | — | — | — |
| Recruiter | ✔ (rec templates) | ✔ (rec) | ✔ (rec) | — | — | — | — |
| Finance | — | — | — | — | — | — | — |

---

## 13. Intelligence & Analytics

### 13.1 AI HR Assistant (`AI`)
> "Approve" here means confirming/authorizing AI-assisted output for consequential use (human-in-the-loop, `BR-AI-3`). AI never self-approves.

| Role | View/Use | Create (prompt/draft) | Edit (output) | Delete | Approve (AI output) | Export | Admin |
|------|:--------:|:---------------------:|:-------------:|:------:|:-------------------:|:------:|:-----:|
| Super Admin | ✔ | ✔ | ✔ | — | — | ✔ | ✔ (platform config) |
| Company Admin | ✔ | ✔ | ✔ | — | ✔ | ✔ | ✔ (company config) |
| HR Manager | ✔ | ✔ | ✔ | — | ✔ | ✔ | ✔ (HR scope) |
| HR Executive | ✔ | ✔ | ✔ | — | ✔ (operational) | — | — |
| Manager | ✔ (team scope) | ✔ | ✔ | — | ✔ (team-related) | — | — |
| Team Lead | ✔ (team scope) | ✔ | ✔ | — | — | — | — |
| Employee | ✔ (self scope) | ✔ (self queries) | — | — | — | — | — |
| Recruiter | ✔ (rec scope) | ✔ | ✔ | — | ✔ (rec drafts) | — | — |
| Finance | ✔ (finance scope) | ✔ | ✔ | — | ✔ (finance-related) | — | — |

### 13.2 Reports (`RPT`)
> "Create/Edit/Admin" apply to report definitions/schedules; "View/Export" apply to running/downloading within scope.

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (all-tenants) | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| Company Admin | ✔ (company) | ✔ | ✔ | ✔ | — | ✔ | ✔ |
| HR Manager | ✔ (company) | ✔ | ✔ | — | — | ✔ | — |
| HR Executive | ✔ (operational) | — | — | — | — | ✔ | — |
| Manager | ✔ (team) | — | — | — | — | ✔ (team) | — |
| Team Lead | ✔ (team) | — | — | — | — | — | — |
| Employee | ✔ (self) | — | — | — | — | ✔ (own, e.g. payslip) | — |
| Recruiter | ✔ (rec) | — | — | — | — | ✔ (rec) | — |
| Finance | ✔ (finance/payroll) | ✔ (finance) | ✔ | — | — | ✔ | — |

### 13.3 Dashboard (`DASH`)
> Dashboards are read/personalize only; data is scoped to the role. No record CRUD.

| Role | View | Create | Edit | Delete | Approve | Export | Admin |
|------|:----:|:------:|:----:|:------:|:-------:|:------:|:-----:|
| Super Admin | ✔ (platform) | — | ✔ (own layout) | — | — | ✔ | ✔ (widgets config) |
| Company Admin | ✔ (company) | — | ✔ (own layout) | — | — | ✔ | ✔ (company widgets) |
| HR Manager | ✔ (company) | — | ✔ (own layout) | — | — | ✔ | — |
| HR Executive | ✔ (operational) | — | ✔ (own layout) | — | — | — | — |
| Manager | ✔ (team) | — | ✔ (own layout) | — | — | — | — |
| Team Lead | ✔ (team) | — | ✔ (own layout) | — | — | — | — |
| Employee | ✔ (self) | — | ✔ (own layout) | — | — | — | — |
| Recruiter | ✔ (rec) | — | ✔ (own layout) | — | — | — | — |
| Finance | ✔ (finance) | — | ✔ (own layout) | — | — | ✔ | — |

---

## 14. Governance Notes

- This matrix is the **source of truth** for `AUTHZ` implementation and test cases. Each ✔/— maps to a positive/negative authorization test.
- **Scope enforcement is mandatory in addition to the action grant.** A ✔ never means "all data"; it means "permitted action, within the role's data scope" (`BR-AUTHZ-3`).
- **Super Admin** is deliberately restricted from routine HR data operations (e.g., no Create/Edit on employee attendance/leave beyond configuration) to preserve tenant trust; cross-tenant access is exceptional and audited.
- **Custom roles** and per-company overrides are permitted (`FR-AUTHZ-4`) but MUST NOT exceed platform baselines or violate tenant isolation.
- Any change to this matrix is a security-relevant change and follows the document-control process; implementations MUST be updated to match.

---

*End of document. Living document; the authoritative RBAC reference for the HRMS.*
