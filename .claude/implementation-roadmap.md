# Implementation Roadmap

## Objective

Implement the Enterprise HR Operations Platform in small, production-ready, independently testable modules.

Never attempt to build the entire application in one step.

Every module must be completed, reviewed, tested, and documented before moving to the next.

---

# General Rules

For every module:

1. Read CLAUDE.md.
2. Read project-rules.md.
3. Read requirements.md.
4. Read architecture.md.
5. Read database.md.
6. Read api-spec.md.
7. Analyze the module.
8. Identify missing requirements.
9. Suggest improvements.
10. Wait for approval if major architectural changes are required.
11. Implement the module.
12. Test the module.
13. Refactor if necessary.
14. Update documentation.

Never skip any step.

---

# Phase 1

Project Initialization

Tasks

- Create project structure
- Configure Spring Boot
- Configure React
- Configure PostgreSQL
- Configure Docker
- Configure Redis
- Configure Swagger
- Configure Validation
- Configure Logging
- Configure Exception Handling

Deliverable

Running project with clean architecture.

---

# Phase 2

Authentication

Tasks

- User entity
- Role entity
- Permission entity
- JWT
- Refresh Token
- Login
- Logout
- Password Reset
- Email Verification
- RBAC

Deliverable

Production-ready authentication.

---

# Phase 3

Organization Management

Tasks

- Company
- Departments
- Designations
- Employment Types
- Settings

---

# Phase 4

Employee Management

Tasks

- Employee CRUD
- Timeline
- Salary Information
- Government IDs
- Social Profiles

---

# Phase 5

Document Management

Tasks

- Upload
- Download
- Preview
- Storage

---

# Phase 6

Attendance

Tasks

- Clock In
- Clock Out
- Wi-Fi/IP Validation
- WFH Workflow
- Corrections

---

# Phase 7

Leave

Tasks

- Leave Types
- Approval
- Calendar
- Balance

---

# Phase 8

Salary

Tasks

- Salary Structure
- Salary History
- Salary Slips

---

# Phase 9

Reimbursement

Tasks

- Claims
- Bill Upload
- Approval

---

# Phase 10

Communication

Tasks

- Announcements
- Notifications
- Email
- WhatsApp

---

# Phase 11

Dashboards

Tasks

- HR Dashboard
- Employee Dashboard
- Reports

---

# Phase 12

AI Assistant

Tasks

- Tool Calling Layer
- Employee Search
- Attendance Search
- Report Generation

---

# Phase 13

Audit & Logging

Tasks

- Audit Logs
- Activity History
- System Logs

---

# Phase 14

Optimization

Tasks

- Performance
- Redis
- Security
- Refactoring

---

# Phase 15

Testing

Tasks

- Unit Tests
- Integration Tests
- API Tests

---

# Phase 16

Deployment

Tasks

- Docker
- CI/CD
- Production Configuration

---

# Definition of Done

A module is complete only if:

- Code is production-ready.
- APIs are documented.
- Tests pass.
- Security is reviewed.
- RBAC is enforced.
- Validation is complete.
- Logging is implemented.
- Audit events are generated.
- Documentation is updated.
- No known critical bugs remain.

---

# Claude Enhancement

Always use your full Claude Code expertise to improve the implementation plan.

Before implementing any module:

- Challenge the design.
- Suggest better architecture if appropriate.
- Identify missing functionality.
- Consider scalability.
- Consider maintainability.
- Consider security.
- Consider future expansion.

Never sacrifice long-term quality for short-term speed.
# Git & GitHub Workflow

## Objective

Maintain a clean, professional Git history throughout the development of the Enterprise HR Operations Platform.

Every completed module should be committed, documented, and pushed to GitHub before starting the next module.

Never accumulate multiple unrelated modules into a single commit.

---

# Workflow

For every completed module:

1. Verify the implementation.
2. Run all tests.
3. Fix warnings and issues.
4. Update documentation.
5. Create a meaningful Git commit.
6. Push the latest changes to GitHub.
7. Confirm the repository is synchronized before starting the next module.

Never start a new module with uncommitted changes.

---

# Commit Standards

Every commit should represent one logical unit of work.

Examples:

feat(auth): implement JWT authentication

feat(employee): implement employee CRUD

feat(attendance): implement Wi-Fi/IP attendance

feat(leave): implement leave approval workflow

feat(document): implement document management

feat(salary): implement salary structure

feat(notification): implement notification center

feat(ai): implement AI tool calling layer

fix(employee): resolve duplicate employee validation

refactor(attendance): simplify attendance workflow

docs(api): update Swagger documentation

test(auth): add authentication integration tests

---

# Pull Request Standards

Before creating a Pull Request:

- Review the implementation.
- Verify coding standards.
- Verify architecture.
- Verify RBAC.
- Verify validations.
- Verify tests.
- Verify documentation.

---

# GitHub Repository Rules

Always keep the repository in a buildable state.

The main branch should always compile successfully.

Never push broken code.

Never push unfinished features.

Never leave TODO implementations.

---

# Documentation Update

Every completed module must update:

- requirements.md
- architecture.md (if changed)
- database.md (if changed)
- api-spec.md (if changed)
- decisions.md (if changed)

Documentation should always match the implementation.

---

# Claude Code Git Instructions

Whenever a module is fully completed:

1. Review the implementation.
2. Ensure the project builds successfully.
3. Ensure tests pass.
4. Stage all relevant files.
5. Create a meaningful commit message.
6. Push the commit to the configured GitHub repository.
7. Verify the push completed successfully.
8. Only then begin the next module.

If Git is not configured, or if pushing requires user authentication or approval, pause and ask for the necessary credentials or confirmation before attempting the push.

Never force-push unless explicitly instructed.

Never rewrite Git history unless explicitly instructed.

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