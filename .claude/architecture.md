# Enterprise HR Operations Platform Architecture

## Objective

Design a production-grade enterprise architecture for this HR Operations Platform.

Do NOT generate code.

This document should become the blueprint for the next 10 years.

Think like a Principal Staff Engineer at Google, Microsoft, Workday, Atlassian, or Amazon.

Every decision should prioritize:

- Scalability
- Maintainability
- Security
- Performance
- Extensibility
- Modularity
- Testability

---

# Design Deliverables

Before implementation, completely design the system.

Produce:

## 1. High-Level Architecture

- System Context Diagram
- High-Level Components
- Request Flow
- Module Communication

---

## 2. Low-Level Architecture

Design every module internally.

Show:

- Responsibilities
- Dependencies
- Boundaries
- Service interactions

---

## 3. Module Boundaries

Define clear ownership for every module.

No circular dependencies.

No shared business logic.

---

## 4. Feature-Based Folder Structure

Design the complete backend folder structure.

Every module should contain:

- Controller
- DTO
- Service
- Repository
- Entity
- Mapper
- Validation
- Exception
- Configuration

---

## 5. Frontend Architecture

Design:

- Pages
- Layouts
- Components
- Shared Components
- Hooks
- Services
- State Management
- Routing

---

## 6. Database Architecture

Define:

- Entity Relationships
- Aggregate Boundaries
- Naming Standards

Do not create tables yet.

---

## 7. API Architecture

Define:

- REST Standards
- URL Naming
- Versioning
- Response Format
- Error Format

---

## 8. Authentication Flow

Design:

- Login Flow
- JWT Flow
- Refresh Token Flow
- Logout
- Password Reset
- Email Verification

---

## 9. Authorization

Design:

- RBAC
- Permission Resolution
- Dynamic Permissions

---

## 10. Attendance Architecture

Design:

Office Attendance

↓

Wi-Fi/IP Validation

↓

WFH Detection

↓

Approval Workflow

↓

Attendance Record

↓

Notifications

↓

Audit Log

---

## 11. Leave Workflow

Employee

↓

Manager

↓

HR

↓

Approval

↓

Notification

↓

Audit

---

## 12. Automation Engine

Design an event-driven architecture.

Examples:

EmployeeCreated

↓

Email

↓

WhatsApp

↓

Audit

↓

Dashboard

↓

Activity Timeline

AttendanceCreated

↓

Notifications

↓

Reports

↓

Audit

---

## 13. Notification Architecture

Support

- Email
- WhatsApp
- In-App

Future

- SMS
- Push

---

## 14. AI Architecture

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

The AI must never access repositories directly.

---

## 15. Search Architecture

Global Search

↓

Search Service

↓

Multiple Modules

↓

Aggregated Results

---

## 16. Audit Architecture

Every important action should generate an audit event.

---

## 17. Logging Architecture

Centralized logging.

Structured logs.

---

## 18. Error Handling

Centralized exception handling.

Standard API responses.

---

## 19. Caching Strategy

Define:

- Redis Usage
- Cache Keys
- TTL Strategy

---

## 20. Performance Strategy

Design:

- Pagination
- Lazy Loading
- Efficient Queries
- Async Processing

---

## 21. Security Strategy

Design:

- OWASP Protection
- Rate Limiting
- Secret Management
- Password Policy
- Session Security

---

## 22. Scalability Strategy

Design for:

10 Employees

↓

100 Employees

↓

1,000 Employees

↓

10,000 Employees

↓

100,000 Employees

Without major architectural changes.

---

## 23. Future Expansion

The architecture should easily support future modules:

- Payroll
- Recruitment
- Performance
- LMS
- Finance
- Asset Management
- IT Helpdesk

Without requiring major refactoring.

---

# Final Instructions

Before implementation:

Challenge the architecture.

Identify weaknesses.

Suggest improvements.

Eliminate unnecessary complexity.

Refactor the architecture if a better design exists.

Only after the architecture is finalized should database design begin.
---

# As-Built State (current codebase — 2026-07-19)

> The rules above are the standard to build to. This is the structure that currently exists.

**Implemented classes** (`com.ogm.hrms`):

- `HrmsApplication` — `@SpringBootApplication` entrypoint.
- `config/SecurityConfig` — one `SecurityFilterChain` (public: `/`, `/login`, static assets;
  everything else `authenticated`), form login + logout, `BCryptPasswordEncoder` bean. Written to
  accept a future stateless `/api/**` JWT chain without disturbing this one.
- `controller/HomeController` — `GET /` → Thymeleaf `index`.
- `exception/` — `GlobalExceptionHandler` (`@RestControllerAdvice`: 400 validation / 404 not-found /
  500 fallback), `ApiErrorResponse` (immutable record), `ResourceNotFoundException`.

**Stub packages** (`package-info.java` only, no types yet): `audit, constants, dto, entity, enums,
mapper, repository, security, service, service.impl, util, validation`.

**Deviations / gaps vs. the target architecture above:**

- Auth is **form login**, not JWT/RBAC (see `decisions.md` D-003).
- No service/repository/entity/DTO/mapper layers exist yet — layering is aspirational until modules land.
- MapStruct, Redis, S3, OpenAPI, async/scheduling, event publishing: **not yet wired**.
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
