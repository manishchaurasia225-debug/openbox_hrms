# Coding Standards

## Objective

This document defines the coding standards, best practices, and implementation guidelines for the Enterprise HR Operations Platform.

Every line of code must be production-ready, maintainable, scalable, secure, and easy to understand.

Always prioritize long-term maintainability over short-term convenience.

---

# General Principles

Always follow:

- SOLID Principles
- Clean Architecture
- DRY
- KISS
- YAGNI
- Separation of Concerns
- Single Responsibility Principle

Never sacrifice architecture for speed.

---

# Code Quality

Always write:

- Clean code
- Readable code
- Self-documenting code
- Modular code
- Reusable code
- Testable code

Avoid:

- Duplicate code
- God Classes
- Long Methods
- Magic Numbers
- Hardcoded Values
- Tight Coupling
- Circular Dependencies

---

# Naming Conventions

Use meaningful names.

Examples

EmployeeService

AttendanceController

LeaveRepository

EmployeeResponse

SalaryMapper

Bad examples

DataService

TempController

Utils

Manager2

TestClass

---

# Controllers

Controllers should only:

- Receive requests
- Validate requests
- Call Services
- Return Responses

Controllers must NEVER:

- Access repositories
- Write business logic
- Execute SQL
- Perform calculations

Keep controllers thin.

---

# Services

Services contain all business logic.

Services should:

- Validate business rules
- Execute workflows
- Coordinate repositories
- Publish events

Never place business logic inside controllers or repositories.

---

# Repositories

Repositories only perform database operations.

Repositories must never:

- Execute business rules
- Calculate values
- Trigger notifications

---

# DTOs

Always use:

- Request DTO
- Response DTO

Never expose entities directly.

Use MapStruct for mapping.

---

# Validation

Use Jakarta Bean Validation.

Validate:

- Request Body
- Path Variables
- Query Parameters

Business validation belongs inside the Service layer.

---

# Exception Handling

Use centralized exception handling.

Never expose stack traces.

Return consistent error responses.

---

# Logging

Use structured logging.

Log:

- Errors
- Warnings
- Authentication
- Authorization
- Business Events

Never log:

- Passwords
- Tokens
- Secrets
- Personal sensitive information

---

# Security

Always:

- Validate permissions
- Validate ownership
- Validate tenant isolation
- Sanitize inputs

Never trust client input.

---

# Performance

Avoid:

- N+1 queries
- Unnecessary object creation
- Repeated database queries

Prefer:

- Pagination
- Batch operations
- Lazy loading where appropriate
- Caching where appropriate

---

# Documentation

Public classes and complex business logic should include meaningful documentation.

Keep documentation synchronized with implementation.

---

# Git Standards

Every commit should:

- Solve one logical problem.
- Be small and focused.
- Include a meaningful commit message.

Avoid large unrelated commits.

---

# Code Review Checklist

Before considering any task complete, verify:

- Code compiles successfully.
- No IDE warnings.
- No unused imports.
- No dead code.
- No duplicated logic.
- Proper exception handling.
- Proper validation.
- Proper logging.
- Proper RBAC enforcement.
- Proper transaction management.
- Proper documentation.
- Tests pass.

---

# Claude Code Instructions

For every implementation:

1. Read all project documentation before writing code.
2. Understand the business requirements.
3. Analyze the current codebase.
4. Reuse existing components where possible.
5. Do not duplicate functionality.
6. Follow the established architecture.
7. Suggest improvements if a better design exists.
8. Keep backward compatibility unless instructed otherwise.
9. Refactor only when it improves maintainability.
10. Explain significant architectural decisions.

Before submitting code:

- Review your own implementation critically.
- Look for bugs, edge cases, race conditions, and security issues.
- Check for performance bottlenecks.
- Verify validation and authorization.
- Ensure code follows all project standards.
- Simplify the implementation if possible without reducing quality.
- Recommend improvements if you identify a better approach.

Always use your full Claude Code expertise to produce enterprise-grade, production-ready code.

Never stop at "working code"; strive for the best possible implementation while preserving the project's architecture and business requirements.

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