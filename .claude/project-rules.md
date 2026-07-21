# Project Rules

## Purpose

This document contains project-specific rules, business decisions, constraints, and permanent implementation guidelines for the Enterprise HR Operations Platform.

These rules override assumptions and prevent accidental changes during future development.

If any implementation conflicts with these rules, always follow this document unless the user explicitly changes the requirement.

---

# General Rules

- This is an Internal Enterprise HR Operations Platform.
- This is NOT a SaaS application.
- This is NOT a demo or portfolio project.
- Build every module as production-ready.
- Never remove existing functionality unless explicitly instructed.
- Never introduce features that have previously been removed.
- Always preserve backward compatibility whenever possible.
- Always keep the system modular and extensible.

---

# Architecture Rules

- Follow Feature-Based Modular Architecture.
- Follow Clean Architecture.
- Follow SOLID principles.
- Business logic belongs only in the Service layer.
- Controllers should remain thin.
- Repositories should only handle database access.
- Never expose database entities directly through APIs.
- Use DTOs for all request and response objects.
- Every module must support future expansion.

---

# Attendance Rules

Attendance must use Office Wi-Fi/IP validation.

Do NOT implement GPS or geolocation-based attendance unless explicitly requested.

Attendance Workflow:

Office Wi-Fi/IP
↓

Office Attendance

Outside Office Network
↓

Prompt Employee

Options:

- Work From Home
- Retry Office Network
- Cancel

If Work From Home is selected:

- Capture reason
- Capture work location
- Capture expected working hours
- Record device information
- Record browser information
- Record public IP
- Record attendance source
- Follow company WFH approval policy

Attendance Types:

- Office
- Work From Home
- Client Visit
- Business Travel
- Casual Leave
- Sick Leave
- Earned Leave
- Half Day
- Early Departure
- Comp Off
- Holiday
- Weekend
- Absent

---

# Employee Management Rules

Employee records should include:

- Personal Information
- Contact Information
- Emergency Contacts
- Family Details
- Education
- Experience
- Employment Details
- Salary Information
- Government IDs
- Social Profiles
- Employee Timeline

Employment Information must include:

- Joining Date
- End Date

Social Profiles include:

- LinkedIn
- Instagram
- Facebook
- X

---

# Salary Rules

Salary structure should support:

- Basic Salary
- HRA
- Special Allowance
- Bonus
- Incentives
- Other Allowances

Salary Slips:

- Generate automatically
- Download as PDF
- Email automatically
- Maintain salary history

---

# Reimbursement Rules

Support claims for:

- Travel
- Fuel
- Food
- Internet
- Medical
- Other Expenses

Bill upload is mandatory where applicable.

---

# Communication Rules

Notifications must support:

- In-App
- Email
- WhatsApp

Birthday wishes should be automated.

Festival greetings should be automated.

Welcome messages should be automated.

Reminder messages should be automated.

---

# AI Rules

The AI Assistant must NEVER:

- Access repositories directly.
- Execute SQL directly.
- Bypass authorization.
- Ignore RBAC.

AI Architecture:

User
↓

AI
↓

Intent Detection
↓

Permission Validation
↓

Tool Calling Layer
↓

Business Services
↓

Repositories
↓

Database

Always validate user permissions before executing any AI action.

---

# Security Rules

Never hardcode:

- Permissions
- Departments
- Designations
- Leave Types
- Attendance Policies
- Company Settings

Everything should be configurable through the admin panel.

Always follow OWASP best practices.

---

# Database Rules

Use PostgreSQL.

Prefer normalized schema.

Use foreign keys.

Use indexes appropriately.

Use soft deletes where applicable.

Every major table should include:

- created_at
- updated_at
- created_by
- updated_by

---

# Reporting Rules

Every major module should provide reports where applicable.

Reports should support:

- Filtering
- Sorting
- Search
- Pagination
- Export to PDF
- Export to Excel
- Export to CSV

---

# Audit Rules

Audit the following events:

- Login
- Logout
- Attendance
- Leave
- Employee Updates
- Salary Updates
- Role Changes
- Permission Changes
- Settings Changes
- Downloads
- Document Uploads
- Approvals
- AI Actions

Audit logs should never be editable.

---

# Features Explicitly Removed

Do NOT implement the following unless explicitly requested again:

- GPS / Geo-location attendance
- Shift Management
- Break Time Tracking
- Reporting Manager field in Employee Management
- Confirmation Date field
- Work Location field in Employee Management
- Skills section
- Certificates section
- GitHub profile
- Portfolio profile
- Appointment Letter document
- Education Certificates upload
- Cancelled Cheque upload
- Police Verification upload
- Medical Reports upload
- Custom Documents
- OCR Document Extraction
- Document Version History
- PF Number
- UAN Number
- ESI Number
- Tax Details
- Professional Tax
- Loan Deduction

---

# Future Expansion

The architecture must support adding future modules without major refactoring, including:

- Payroll
- Recruitment
- Performance Management
- Learning Management
- Asset Management
- IT Helpdesk
- Visitor Management
- Travel Management
- Finance
- Procurement

---

# Claude Code Instructions

For every task:

1. Read CLAUDE.md.
2. Read project-rules.md.
3. Read requirements.md.
4. Review architecture.md (when available).
5. Never assume business rules.
6. Identify missing validations and edge cases.
7. Suggest enterprise improvements.
8. Preserve all existing requirements.
9. Do not remove or modify functionality unless explicitly instructed.
10. Use your full Claude Code expertise to deliver production-grade, scalable, secure, maintainable, and enterprise-quality solutions.


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