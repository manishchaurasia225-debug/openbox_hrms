# Enterprise HR Operations Platform

## Project Overview

Build a production-grade Internal HR Operations Platform for an organization.

This is NOT a demo application.

This is NOT an MVP.

This is NOT a SaaS product.

This platform will be used daily by HR, Employees, Managers, and Administrators.

The application should become the company's single source of truth for employee information, HR operations, automation, and reporting.

The project must be scalable, secure, maintainable, configurable, and future-ready.

---

# Core Principles

- Every module should be independently maintainable.
- Business rules should never be hardcoded.
- Permissions must be configurable.
- Workflows must be configurable.
- Every important action must be audited.
- Every module should support search, filtering, sorting, and pagination where applicable.
- Every module should be extensible for future requirements.

---

# User Roles

Current Roles

- Super Admin
- HR Admin
- HR Executive
- Reporting Manager
- Employee

Future Roles

- Finance
- IT Admin

RBAC must be dynamic.

---

# Functional Modules

## Phase 1 — Core Foundation

### Module 1 — System Architecture

Functionalities

- Clean Architecture
- Domain Driven Design
- SOLID Principles
- Modular Feature-Based Design
- Shared Components
- Validation
- Exception Handling
- Logging
- Event Publishing
- API Standards

---

### Module 2 — Authentication & Authorization

Functionalities

- Login
- JWT Authentication
- Refresh Tokens
- Password Reset
- Email Verification
- Optional Two-Factor Authentication
- Session Management
- Login History
- Failed Login Tracking
- Device Tracking
- Logout Everywhere
- Token Revocation
- Dynamic RBAC
- Permission Management
- Account Lock

---

### Module 3 — Organization & System Settings

Functionalities

- Company Profile
- Departments
- Designations
- Employment Types
- Office Locations
- Roles
- Permissions
- Company Branding
- SMTP Settings
- WhatsApp Settings
- Storage Settings
- AI Settings
- Notification Settings

Attendance Policies

- Office Wi-Fi/IP Management
- Working Hours
- WFH Policies
- Maximum WFH Days
- Auto Approval Rules

---

# Phase 2 — Employee Core

### Module 4 — Employee Management

Functionalities

Personal Information

- Employee ID
- Employee Photo
- Full Name
- Gender
- DOB
- Blood Group
- Nationality
- Marital Status

Contact Information

- Mobile
- Personal Email
- Official Email
- Current Address
- Permanent Address

Emergency Contact

Family Details

Education

Experience

Employment Information

- Joining Date
- End Date
- Department
- Designation
- Employment Type
- Notice Period
- Employment Status

Salary Information

- Basic Salary
- HRA
- Special Allowance
- Bonus
- Incentives
- Other Allowances
- Bank Details

Government IDs

- PAN
- Aadhaar
- Passport
- Driving License

Social Profiles

- LinkedIn
- Instagram
- Facebook
- X

Employee Timeline

---

### Module 5 — Document Management

Functionalities

- Resume
- Offer Letter
- Joining Letter
- Experience Letter
- Salary Slips
- Company Policies

Features

- Upload
- Download
- Preview
- Folder Structure
- Metadata
- Expiry Tracking
- Role-Based Access

---

# Phase 3 — Attendance & Leave

### Module 6 — Attendance Management

Functionalities

Office Attendance

- Clock In
- Clock Out
- Working Hours
- Half Day
- Overtime

Smart Wi-Fi/IP Attendance

- Validate Office Wi-Fi/IP
- Automatic Office Attendance
- Outside Office Detection
- Work From Home Workflow
- Retry Office Network
- Device Tracking
- Browser Tracking
- Public IP Logging
- Attendance Source Tracking
- Attendance Corrections

Attendance Types

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

Reports

- Daily
- Monthly
- Attendance History

---

### Module 7 — Leave Management

Functionalities

- Casual Leave
- Sick Leave
- Earned Leave
- LOP
- Work From Home
- Maternity Leave
- Paternity Leave

Features

- Leave Request
- Leave Balance
- Leave Calendar
- Manager Approval
- HR Approval
- Notifications

---

### Module 8 — Holiday Management

Functionalities

- National Holidays
- Regional Holidays
- Company Holidays
- Holiday Calendar

---

# Phase 4 — Employee Communication

### Module 9 — Announcement Management

Functionalities

- Company News
- Events
- Policies
- Holidays
- Office Closures
- Training Announcements

---

### Module 10 — Notification Center

Channels

- In-App
- Email
- WhatsApp
- Push Notification

Features

- Notification History
- Read Status
- Retry Failed Messages

---

# Phase 5 — Finance

### Module 11 — Salary & Compensation

Functionalities

Salary

- Basic Salary
- HRA
- Special Allowance
- Bonus
- Incentives
- Other Allowances
- Bank Details
- Salary History
- Salary Revision

Salary Slips

- Generate PDF
- Download
- Email Automatically
- History

---

### Module 12 — Reimbursement

Functionalities

- Travel
- Fuel
- Internet
- Food
- Medical
- Other Expenses
- Bill Upload
- Claim Submission
- HR Approval
- Approval History

---

# Phase 6 — HR Operations

### Module 13 — Confirmation & Probation

Functionalities

- Probation Tracking
- Confirmation
- Extension
- HR Approval

---

### Module 14 — Employee Lifecycle

Functionalities

Onboarding

- Joining
- Welcome
- Documentation

Offboarding

- Resignation
- Exit Process
- Clearance
- Relieving
- Final Documents

---

# Phase 7 — Analytics

### Module 15 — HR Dashboard

Widgets

- Total Employees
- Office Attendance
- WFH Attendance
- Employees on Leave
- Pending WFH Requests
- Pending Leave Requests
- Birthdays
- Work Anniversaries
- New Joiners
- Department Distribution
- Gender Distribution
- Hiring Trend
- Employee Growth
- Attrition
- Attendance Trend
- Holiday Calendar
- Announcements
- Recent Activities

---

### Module 16 — Employee Dashboard

Functionalities

- Profile Completion
- Attendance Summary
- Attendance History
- Leave Balance
- Holiday Calendar
- Documents
- Salary Slips
- Company Events
- Tasks

---

### Module 17 — Reports

Reports

- Employee
- Attendance
- Leave
- Department
- Salary

Export

- PDF
- Excel
- CSV

---

# Phase 8 — Automation

### Module 18 — Automation Engine

Functionalities

- Birthday Wishes
- Festival Wishes
- Welcome Messages
- Attendance Reminder
- Leave Reminder
- Missing Documents
- Promotion Congratulations
- Confirmation Reminder
- Contract Expiry

Channels

- Email
- WhatsApp
- In-App Notification

---

### Module 19 — Email Template Engine

Functionalities

- HTML Templates
- Dynamic Variables
- Categories
- Preview

---

### Module 20 — WhatsApp Integration

Functionalities

- Meta WhatsApp Business API
- Birthday Messages
- Festival Messages
- Welcome Messages
- Approval Messages
- Reminder Messages
- Delivery Tracking
- Read Status

---

# Phase 9 — AI

### Module 21 — Enterprise AI Assistant

Functionalities

- Employee Search
- Attendance Search
- Leave Search
- Dashboard Summary
- Report Generation
- Analytics
- Policy Search
- Document Search

The AI must never access repositories or databases directly.

It must use a Tool Calling Layer and Business Services.

---

### Module 22 — Global Search

Search By

- Employee Name
- Employee ID
- Mobile
- Email
- Department
- Documents
- Government IDs

---

# Phase 10 — Governance

### Module 23 — Audit Logs

Track

- Login
- Logout
- Attendance
- Leave
- Employee Updates
- Salary Updates
- Downloads
- Settings
- Permission Changes
- Approvals

---

### Module 24 — System Administration

Functionalities

- User Management
- Role Management
- Permission Management
- Feature Flags
- Backup Settings
- Configuration Management

---

# Instructions for Claude

Before implementing ANY module:

1. Analyze the module thoroughly.
2. Identify missing business requirements.
3. Identify missing workflows.
4. Identify validations.
5. Identify edge cases.
6. Identify approval flows.
7. Identify notification requirements.
8. Identify reports required.
9. Suggest enterprise improvements.
10. Ask questions if business rules are ambiguous.

Only after the module requirements are finalized should implementation begin.

Never skip requirement analysis.

Never assume business rules.

Always think like a Principal Software Architect building an enterprise HRMS that will be maintained for the next 10 years.
---

# As-Built State (current codebase — 2026-07-19)

> The modules above are **target scope**. This is what is actually implemented today.

- **Delivered:** project foundation only — Spring Security shell (form login + BCrypt encoder),
  centralized exception handling (`GlobalExceptionHandler` + `ApiErrorResponse` + `ResourceNotFoundException`),
  a Thymeleaf home page (`GET /`), dev/prod profiles, run scripts, `.env.example`, the product SRS
  under `docs/01-product/`, and a Postman collection.
- **Not yet started:** every functional module (Auth/JWT, RBAC, Organization/Settings, Employee,
  Documents, Attendance, Leave, Holiday, Announcements, Notifications, Salary, Reimbursement,
  Confirmation, Lifecycle, Dashboards, Reports, Automation, AI Assistant, Search).
- **RBAC:** authoritative matrix exists as a spec (`docs/01-product/permissions-matrix.md`) but is
  **not implemented** — current security is coarse (public endpoints vs `authenticated`).
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
- Delivery order: see `implementation-roadmap.md`.
