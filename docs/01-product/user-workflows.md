# HRMS — User Workflows

> **Document type:** Process / Workflow Specification
> **Product:** OGM Human Resource Management System (HRMS)
> **Document ID:** `01-product/user-workflows`
> **Status:** Draft v1.0 (Foundation)
> **Owner:** Product Management
> **Audience:** Senior Software Engineers, Product Managers, Architects, QA
> **Last updated:** 2026-07-16
> **Traces to:** [`vision-and-business-goals`](./vision-and-business-goals.md), [`functional-requirements`](./functional-requirements.md)

---

## Document Control

| Version | Date       | Author  | Change summary                     |
|--------:|------------|---------|-------------------------------------|
| 1.0     | 2026-07-16 | Product | Initial workflow baseline (11 flows) |

---

## 1. Introduction

This document defines the primary end-to-end user workflows of the HRMS. Each workflow uses a consistent structure so engineering and QA can implement and test the happy path plus the realistic deviations.

**Workflow template**
- **Goal** — the outcome the workflow achieves.
- **Primary actor(s)** — who drives it (roles from the stakeholder model).
- **Preconditions** — what must be true before starting.
- **Trigger** — the event that starts the flow.
- **Main flow** — numbered happy-path steps.
- **Alternate / exception flows** — realistic deviations (rejections, errors, edge cases).
- **Postconditions** — the resulting system state.
- **Modules involved** — HRMS modules (per functional-requirements) touched.

> Conventions: Steps are numbered `1, 2, 3…`. Alternate flows branch as `An` (e.g., `A2` = alternate at step 2). All flows assume the actor is authenticated (`AUTH`) and authorized (`AUTHZ`), and all sensitive actions are audited (`AUDIT`). These are not repeated in each flow.

---

## 2. Workflow: Employee Onboarding

**Goal:** Bring a new hire from "offer accepted" to a fully set-up, active employee.
**Primary actors:** HR Executive (driver), New Employee, Reporting Manager, IT/Asset custodian.
**Preconditions:** Company, branch, department, and designation exist; candidate/hire data available (possibly from `REC`).
**Trigger:** A candidate is marked **hired**, or HR initiates onboarding for a new joiner.

**Main flow**
1. HR Executive initiates onboarding; if the person came from Recruitment, their data is carried over (no re-entry).
2. HR completes/verifies the employee master record (personal, employment, statutory, bank) — `EMP`.
3. System assigns company, branch, department, designation, and reporting manager.
4. HR requests required documents; employee uploads them — `DOC`.
5. System provisions the employee account and role(s) — `AUTH`, `AUTHZ`.
6. HR assigns applicable shift and leave policy — `SHIFT`, `LEAVE`, `SET`.
7. Assets are issued (laptop, access card, etc.) — `ASSET`.
8. Onboarding tasks/checklist are completed (policy acknowledgements, orientation) — `DOC`, `ANN`.
9. System notifies the employee (credentials/first-day info) and the manager — `NOTIF`, `EMAIL`.
10. Employee status is set to **active**; the record becomes referenceable across modules.

**Alternate / exception flows**
- `A2` Missing mandatory data → system blocks activation and lists gaps; HR completes before proceeding.
- `A4` Required documents not submitted → onboarding remains **in progress**; reminders sent — `NOTIF`.
- `A5` Account provisioning conflict (duplicate identifier) → system flags; HR resolves before continuing.
- `A7` Asset unavailable → asset step marked pending; does not block activation unless policy requires.

**Postconditions:** Active employee with account, role, org placement, policies, documents, and assets recorded. Onboarding time measured (M3).
**Modules involved:** `REC`, `EMP`, `DOC`, `AUTH`, `AUTHZ`, `SHIFT`, `LEAVE`, `ASSET`, `NOTIF`, `EMAIL`, `SET`, `AUDIT`.

---

## 3. Workflow: Employee Resignation / Offboarding

**Goal:** Cleanly exit an employee, recovering assets, settling dues, and revoking access.
**Primary actors:** Employee (initiator), Reporting Manager, HR Manager/Executive, Finance.
**Preconditions:** Active employee.
**Trigger:** Employee submits resignation, or HR initiates termination/exit.

**Main flow**
1. Employee submits resignation with intended last working day — `EMP`.
2. Manager and HR acknowledge; notice period is computed per policy — `SET`.
3. Employee status changes to **on-notice**; exit checklist is generated.
4. Knowledge transfer and pending-task handover are tracked.
5. Asset return is verified; outstanding assets are flagged — `ASSET`.
6. Final leave balance and attendance are reconciled — `LEAVE`, `ATT`.
7. Final settlement is computed (dues, recoveries, unpaid balances) — `PAY`, `EXP`.
8. On last working day, access and account are revoked — `AUTH`, `AUTHZ`.
9. Employee status is set to **exited**; records are retained (soft), not deleted — `EMP`, `AUDIT`.
10. Exit documents (relieving/experience letters) are issued — `DOC`, `EMAIL`.

**Alternate / exception flows**
- `A1` Resignation withdrawn before acceptance → status reverts to **active** per policy.
- `A5` Assets not returned → settlement holds or recovers value per policy; flagged for Finance.
- `A7` Pending reimbursements/dues → included in final settlement before closure.
- `A2` Involuntary termination path → HR initiates directly; steps 5–10 apply with appropriate approvals.

**Postconditions:** Employee **exited**, access revoked, assets and dues settled, records retained for audit/statutory needs.
**Modules involved:** `EMP`, `ASSET`, `LEAVE`, `ATT`, `PAY`, `EXP`, `AUTH`, `AUTHZ`, `DOC`, `NOTIF`, `EMAIL`, `AUDIT`.

---

## 4. Workflow: Attendance Marking (Wi-Fi/IP based)

**Goal:** Record valid attendance automatically based on presence on an approved workplace network.
**Primary actors:** Employee; HR Executive (exceptions).
**Preconditions:** Active employee with an assigned shift; branch has approved network identifiers configured — `BRANCH`, `SHIFT`.
**Trigger:** Employee checks in/out (or the client detects an approved network).

**Main flow**
1. Employee initiates check-in on the app/portal.
2. System captures the network identifier (Wi-Fi SSID/BSSID or IP) and timestamp — `ATT`.
3. System validates the identifier against the branch's approved list — `BRANCH`.
4. If valid, the check-in is recorded as **validated**.
5. At check-out, the same validation records the end event.
6. System derives daily status (present/late/half-day) using the effective shift and grace — `SHIFT`.
7. Holidays/weekends are respected in status derivation — `HOL`.
8. Attendance contributes to payroll for the period — `PAY`.

**Alternate / exception flows**
- `A3` Network not approved → check-in flagged/blocked per policy; not silently accepted.
- `A1` Missed punch → employee raises a **regularization** request; routes to approver; on approval the day updates — `ATT`, `NOTIF`.
- `A2` Duplicate/rapid punches → de-duplicated per rules.
- `A6` Employee on approved leave → day reflects leave, not absence — `LEAVE`.
- `A0` Period already finalized for payroll → attendance is locked; corrections go to a subsequent adjustment — `PAY`.

**Postconditions:** Accurate daily attendance recorded; exceptions surfaced for review; payroll inputs prepared.
**Modules involved:** `ATT`, `BRANCH`, `SHIFT`, `HOL`, `LEAVE`, `PAY`, `NOTIF`, `AUDIT`.

---

## 5. Workflow: Leave Application

**Goal:** Let an employee request time off with correct balance handling.
**Primary actors:** Employee (initiator); Approver (Manager/HR) — see [Leave Approval](#6-workflow-leave-approval).
**Preconditions:** Active employee with a leave policy and balances — `LEAVE`, `SET`.
**Trigger:** Employee applies for leave.

**Main flow**
1. Employee selects leave type, dates, and reason — `LEAVE`.
2. System checks balance and eligibility for the selected type.
3. System provisionally holds the balance to prevent overdraw — `BR-LEAVE-1`.
4. System routes the request to the appropriate approver(s) per hierarchy/policy — `AUTHZ`.
5. Approver and employee are notified of the pending request — `NOTIF`.
6. Request status is **pending** until decided.

**Alternate / exception flows**
- `A2` Insufficient balance for a paid type → system blocks or offers unpaid alternative per policy.
- `A2` Overlaps existing approved leave/holiday → flagged; handled per leave-type policy — `HOL`.
- `A1` Half-day / partial-day request → handled per policy configuration.
- `A6` Employee cancels before decision → provisional hold released.

**Postconditions:** A pending leave request with a provisional balance hold, routed for approval.
**Modules involved:** `LEAVE`, `HOL`, `AUTHZ`, `NOTIF`, `AUDIT`.

---

## 6. Workflow: Leave Approval

**Goal:** Decide a pending leave request and apply its effects.
**Primary actors:** Approver (Manager/Team Lead/HR).
**Preconditions:** A pending leave request exists (from [Leave Application](#5-workflow-leave-application)).
**Trigger:** Approver opens the pending request.

**Main flow**
1. Approver reviews the request with team-coverage/calendar context — `LEAVE`, `DASH`.
2. Approver approves or rejects with an optional note.
3. **On approval:** balance is deducted, attendance reflects the leave, and payroll consumes the outcome — `ATT`, `PAY`.
4. **On rejection:** provisional hold is released; no balance change.
5. Employee is notified of the decision — `NOTIF`, optionally `EMAIL`/`WA`.
6. Request status becomes **approved** or **rejected**.

**Alternate / exception flows**
- `A1` Multi-level approval required → moves to the next approver until fully approved — `AUTHZ`.
- `A2` Approver requests changes → returns to employee for modification/resubmission.
- `A0` No action within SLA → escalation/reminder per policy (M4 turnaround) — `NOTIF`.
- `A3` Period already finalized in payroll → effect applies to the next cycle as an adjustment.

**Postconditions:** Leave decided; balances, attendance, and payroll consistent with the decision.
**Modules involved:** `LEAVE`, `ATT`, `PAY`, `AUTHZ`, `NOTIF`, `DASH`, `AUDIT`.

---

## 7. Workflow: Payroll Generation

**Goal:** Produce accurate, approved, auditable payroll for a cycle and hand off disbursement to Finance.
**Primary actors:** HR Manager (driver), Finance, Employees (recipients).
**Preconditions:** Employees have salary structures; attendance and leave for the period are ready to finalize — `PAY`, `EMP`, `ATT`, `LEAVE`.
**Trigger:** Payroll cycle reaches its run point.

**Main flow**
1. HR initiates the payroll run for the cycle — `PAY`.
2. System finalizes/locks attendance and leave for the period — `BR-PAY-1`.
3. System computes earnings, deductions, statutory components, loss-of-pay, and net pay per employee — with a transparent breakdown.
4. HR reviews the run and exception report (anomalies flagged, optionally by `AI`).
5. HR corrects source data if needed (unlock → fix → re-run) before finalization.
6. Authorized approver finalizes the run — it becomes **locked/immutable** — `BR-PAY-2`.
7. Payslips are generated and made available to employees — `DOC`, `NOTIF`, `EMAIL`.
8. Disbursement output is provided to Finance; disbursement status is recorded — `EXP`/Finance.
9. Payroll history is retained for audit and statutory purposes — `AUDIT`, `RPT`.

**Alternate / exception flows**
- `A3` Calculation anomaly detected → flagged for human review before finalization; nothing auto-finalizes.
- `A2` Attendance/leave not ready → run is blocked until inputs are finalized.
- `A6` Post-finalization correction needed → handled as an adjustment in the next cycle, fully audited (no edits to the locked run).
- `A8` Disbursement failure for an employee → recorded and retried; does not silently drop.

**Postconditions:** Finalized, locked payroll; payslips issued; Finance has disbursement data; full audit trail. Processing time (M2) and accuracy (M8) measured.
**Modules involved:** `PAY`, `EMP`, `ATT`, `LEAVE`, `HOL`, `SHIFT`, `DOC`, `AI`, `NOTIF`, `EMAIL`, `RPT`, `AUDIT`.

---

## 8. Workflow: Recruitment Lifecycle

**Goal:** Move from an approved job requisition to a hired candidate ready for onboarding.
**Primary actors:** Recruiter (driver), Hiring Manager, Candidate, HR.
**Preconditions:** Department and designation exist; requisition approved — `DEPT`, `DESIG`, `REC`.
**Trigger:** A hiring need is raised as a requisition.

**Main flow**
1. Hiring Manager/HR raises a requisition; it is approved before publishing — `REC`, `AUTHZ`.
2. Recruiter publishes the role; candidates apply via the candidate portal — `REC`.
3. Candidates progress through pipeline stages: applied → screened → interview → offer.
4. Recruiter schedules interviews; interviewers submit structured feedback — `REC`, `NOTIF`, `EMAIL`/`WA`.
5. (Optional) `AI` assists with JD drafting and candidate summarization — human decides — `AI`.
6. Hiring decision is made; an offer is extended to the selected candidate.
7. Candidate accepts; recruiter marks the candidate **hired**.
8. The hired candidate is converted into an onboarding employee record — `EMP` (see [Employee Onboarding](#2-workflow-employee-onboarding)).

**Alternate / exception flows**
- `A1` Requisition rejected → not published; returns to requester.
- `A3` Candidate rejected at any stage → status updated; candidate notified; data retained per policy.
- `A6` Candidate declines offer → pipeline continues with other candidates; requisition stays open.
- `A5` AI suggestion used → labeled and human-reviewed; never auto-rejects (`BR-REC-3`).

**Postconditions:** Role filled with a hired candidate converted to onboarding, or requisition remains open; pipeline history retained. Time-to-hire measured (M9).
**Modules involved:** `REC`, `DEPT`, `DESIG`, `EMP`, `DOC`, `AI`, `NOTIF`, `EMAIL`, `WA`, `AUTHZ`, `AUDIT`.

---

## 9. Workflow: Performance Review

**Goal:** Complete a structured performance cycle producing recorded outcomes.
**Primary actors:** Employee, Manager; HR (cycle owner).
**Preconditions:** An active performance cycle with goals defined — `PERF`.
**Trigger:** HR opens a review cycle.

**Main flow**
1. HR launches the cycle and notifies participants — `PERF`, `NOTIF`.
2. Employees complete self-review against goals — `PERF`.
3. Managers review reports, provide ratings and comments — `PERF`, `AUTHZ` (scope).
4. (Optional) 360/peer feedback is collected where configured.
5. (Optional) `AI` summarizes inputs; ratings remain human-authored — `AI` (`BR-PERF-2`).
6. Manager finalizes the review; outcome is recorded and shared with the employee.
7. Outcomes feed reporting and downstream decisions (development, compensation inputs) — `RPT`, `DASH`.

**Alternate / exception flows**
- `A2` Self-review not submitted by deadline → escalation/reminders; manager may proceed per policy.
- `A3` Rating disputed → acknowledgement/appeal step per policy; recorded.
- `A0` Cycle extended → deadlines updated; participants notified.

**Postconditions:** Completed reviews with recorded, scope-appropriate outcomes; inputs available to authorized roles.
**Modules involved:** `PERF`, `EMP`, `DEPT`, `AUTHZ`, `AI`, `NOTIF`, `RPT`, `DASH`, `AUDIT`.

---

## 10. Workflow: Expense Reimbursement

**Goal:** Reimburse an employee for a valid, approved expense.
**Primary actors:** Employee (claimant), Approver (Manager/Finance), Finance.
**Preconditions:** Active employee; expense policy and limits configured — `EXP`, `SET`.
**Trigger:** Employee submits an expense claim.

**Main flow**
1. Employee submits a claim with category, amount, and receipts — `EXP`, `DOC`.
2. System validates against policy limits and required documentation — `BR-EXP-1`.
3. Claim routes to the approver(s) per policy and amount thresholds — `AUTHZ`, `NOTIF`.
4. Approver approves or rejects with a note.
5. **On approval:** claim moves to reimbursement; Finance processes payout (standalone or via payroll) — `PAY`/Finance.
6. Reimbursement status is recorded; the employee is notified — `NOTIF`.
7. Reimbursed claim is locked and auditable — `BR-EXP-3`.

**Alternate / exception flows**
- `A2` Missing/invalid receipts → claim returned for correction.
- `A2` Amount exceeds limit → requires higher approval or is rejected per policy.
- `A4` Rejected → employee notified with reason; claim closed.
- `A5` Payout failure → recorded and retried; not silently dropped.

**Postconditions:** Claim resolved (reimbursed or rejected); financial and audit records consistent.
**Modules involved:** `EXP`, `DOC`, `AUTHZ`, `PAY`, `NOTIF`, `RPT`, `AUDIT`.

---

## 11. Workflow: Document Approval

**Goal:** Route a document requiring approval/acknowledgement to the right party and record the outcome.
**Primary actors:** Submitter (Employee/HR), Approver (HR/Manager).
**Preconditions:** Document category requires approval or acknowledgement — `DOC`, `SET`.
**Trigger:** A document is uploaded/assigned that needs approval or acknowledgement.

**Main flow**
1. Submitter uploads the document with category and metadata — `DOC`.
2. System routes it to the required approver(s) per category rules — `AUTHZ`, `NOTIF`.
3. Approver reviews; approves, rejects, or requests changes.
4. **On approval/acknowledgement:** status is recorded with timestamp and actor — `AUDIT`.
5. Relevant parties are notified of the outcome — `NOTIF`.
6. The document becomes available to authorized users per RBAC.

**Alternate / exception flows**
- `A3` Changes requested → returns to submitter for revision and resubmission (versioned) — `FR-DOC-3`.
- `A2` Unauthorized access attempt during review → denied and audited — `BR-DOC-1`.
- `A0` Document has an expiry (e.g., certification) → renewal reminder scheduled — `NOTIF`.

**Postconditions:** Document approved/acknowledged (or rejected) with an immutable record; access controlled by RBAC.
**Modules involved:** `DOC`, `AUTHZ`, `NOTIF`, `AUDIT`, `SET`.

---

## 12. Workflow: Announcement Publishing

**Goal:** Publish a targeted announcement to the correct audience across chosen channels.
**Primary actors:** Author (HR Manager/Company Admin/Manager per scope).
**Preconditions:** Author authorized to publish to the target scope — `ANN`, `AUTHZ`.
**Trigger:** Author creates an announcement.

**Main flow**
1. Author composes the announcement and selects the target audience (company/branch/department/role) — `ANN`.
2. Author selects delivery channels (in-app and optionally `EMAIL`/`WA`) and optional schedule.
3. System validates targeting against the author's scope and tenant boundary — `BR-ANN-2`.
4. **On publish (or at scheduled time):** the announcement appears in recipients' feeds and dispatches to selected channels — `NOTIF`, `EMAIL`, `WA`.
5. Read/engagement is tracked where supported; the announcement respects its expiry.

**Alternate / exception flows**
- `A3` Target audience exceeds author's scope → blocked; author narrows scope.
- `A2` Scheduled announcement → queued and published at the scheduled time; editable/cancelable before then.
- `A4` External channel delivery failure → logged and retried; in-app delivery still succeeds — `NOTIF`.
- `A4` WhatsApp recipients without consent → suppressed for that channel (`BR-WA-1`).

**Postconditions:** Correctly-scoped audience receives the announcement via chosen channels; delivery is auditable.
**Modules involved:** `ANN`, `AUTHZ`, `NOTIF`, `EMAIL`, `WA`, `AUDIT`.

---

## 13. Cross-Workflow Notes

- **Approvals** across leave, expense, document, requisition, and payroll share a common pattern: route by RBAC scope → notify → decide → apply effects → audit. A shared **Approval Workflow** concept (see glossary) underpins them.
- **Notifications** (`NOTIF`) and **Audit** (`AUDIT`) participate in essentially every workflow and are assumed, not re-stated, at each step.
- **Locking semantics** (attendance/payroll periods) protect financial integrity; corrections after locking always occur as audited adjustments, never in-place edits.

---

*End of document. Living document; changes follow the document-control process and must stay consistent with the functional requirements, permissions matrix, and glossary.*
