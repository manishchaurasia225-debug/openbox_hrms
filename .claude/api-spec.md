# API Specification

## Objective

Design a production-grade REST API specification for the Enterprise HR Operations Platform.

Do NOT generate controller implementations immediately.

First define the API contract.

The API should be:

- RESTful
- Versioned
- Secure
- Consistent
- Backward compatible
- Easy to consume
- Well documented

---

# API Standards

Base URL

/api/v1

Future versions

/api/v2

Never expose internal implementation details.

Never expose entities directly.

Always use DTOs.

---

# HTTP Methods

GET

Retrieve resources.

POST

Create resources.

PUT

Replace resources.

PATCH

Partial updates.

DELETE

Soft delete wherever applicable.

---

# Standard Response Format

Every API should return a consistent response structure.

Example:

{
"success": true,
"message": "Employee created successfully.",
"data": { ... },
"timestamp": "...",
"requestId": "...",
"errors": null
}

Error Example

{
"success": false,
"message": "Validation failed.",
"errors": [
{
"field": "email",
"message": "Email already exists."
}
],
"timestamp": "...",
"requestId": "..."
}

---

# Authentication

Protected endpoints require JWT.

Authentication endpoints:

- Login
- Logout
- Refresh Token
- Forgot Password
- Reset Password
- Verify Email

---

# Authorization

Every endpoint must define:

- Required Role(s)
- Required Permission(s)

RBAC must be enforced before business logic.

---

# API Design Guidelines

For every endpoint define:

- Endpoint URL
- HTTP Method
- Description
- Request DTO
- Response DTO
- Validation Rules
- Authentication Requirement
- Authorization Requirement
- Possible Errors
- Success Response
- Pagination Support
- Filtering Support
- Sorting Support
- Search Support

---

# Module APIs

Design APIs for:

## Authentication

- Login
- Logout
- Refresh Token
- Forgot Password
- Reset Password
- Verify Email

---

## Employee

- Create
- Update
- Get by ID
- Get by Employee ID
- Search
- List
- Delete (Soft)
- Activate
- Deactivate

---

## Attendance

- Clock In
- Clock Out
- Attendance History
- Attendance Correction
- WFH Request
- Attendance Reports

---

## Leave

- Apply Leave
- Cancel Leave
- Approve
- Reject
- Leave Balance
- Leave History

---

## Salary

- Salary Structure
- Salary History
- Salary Slip
- Download Salary Slip

---

## Documents

- Upload
- Download
- Preview
- Delete
- Search

---

## Reimbursement

- Submit Claim
- Upload Bill
- Approve
- Reject
- History

---

## Announcements

- Create
- Publish
- Update
- Delete
- List

---

## Notifications

- List
- Mark Read
- Mark All Read
- Delete

---

## Dashboard

- HR Dashboard
- Employee Dashboard
- Analytics
- Reports

---

## Reports

- Employee
- Attendance
- Leave
- Salary
- Reimbursement

---

## AI Assistant

- Chat
- Tool Execution
- Conversation History

---

# Validation

Every endpoint must validate:

- Request Body
- Query Parameters
- Path Variables

Use Bean Validation for input validation.

Business validations belong in the Service layer.

---

# Pagination

Support pagination for all list APIs.

Parameters:

page
size
sort

---

# Filtering

Support dynamic filtering where applicable.

Examples:

Department

Status

Date Range

Employment Type

Attendance Type

---

# Search

Support keyword search wherever appropriate.

---

# File Upload

Use multipart/form-data.

Validate:

- File Type
- File Size
- Virus Scan Hook
- Storage Path

---

# Error Handling

Use standard HTTP status codes.

Examples:

200 OK

201 Created

204 No Content

400 Bad Request

401 Unauthorized

403 Forbidden

404 Not Found

409 Conflict

422 Validation Failed

500 Internal Server Error

---

# API Documentation

Every endpoint must include:

- Description
- Request Example
- Response Example
- Error Example

Generate complete OpenAPI / Swagger documentation.

---

# Versioning

All APIs must be versioned.

Breaking changes require a new version.

---

# Claude Enhancement

Always use your full Claude Code expertise to improve API design before implementation.

Review every endpoint for:

- Missing operations
- Missing validations
- Security concerns
- RBAC enforcement
- Performance
- Idempotency
- Pagination
- Filtering
- Search
- Error handling
- Future extensibility

If a better API design exists, recommend it before generating code.
---

# As-Built State (current codebase — 2026-07-19)

> The contract above is the target. This is what the service actually exposes today.

**Live endpoints (only):**

| Method | Path | Handler | Notes |
|--------|------|---------|-------|
| GET | `/` | `HomeController.home` | Thymeleaf `index` (HTML, public) |
| GET/POST | `/login` | Spring Security | Default form-login (CSRF enabled) |
| POST | `/logout` | Spring Security | Session invalidation |

**No `/api/v1/**` REST endpoints exist yet.** No JWT, no versioned API, no pagination/filter/search
infrastructure — all target state.

**Actual error contract (differs from the target envelope above):** the implemented `ApiErrorResponse`
record is:

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request",
  "message": "...", "path": "/...", "validationErrors": { "field": "msg" } }
```

Not the `{success, message, data, timestamp, requestId, errors}` envelope specified above.
**Reconcile before building the first REST controller** — pick one shape and update the other doc
(tracked as an open item; see `decisions.md`). `validationErrors` is present only on validation
failures (`@JsonInclude(NON_NULL)`). Handled centrally by `GlobalExceptionHandler`
(400 validation / 404 `ResourceNotFoundException` / 500 fallback, no stack traces to clients).


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
