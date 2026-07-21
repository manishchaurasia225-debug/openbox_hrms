# Testing

## Stack

JUnit 5 (Jupiter) · Spring Boot modular test starters (`*-test`) · `spring-security-test` ·
AssertJ · Hamcrest · JSONPath. Target additions: Mockito (unit), **Testcontainers (PostgreSQL)**
for integration tests against a real DB.

## Test pyramid

1. **Unit** — pure JUnit + Mockito on services, mappers, validators, utilities. No Spring context.
   Fast, the bulk of coverage. Design for this: constructor injection, no static dependencies.
2. **Slice** — `@WebMvcTest` (controllers + security, no DB), `@DataJpaTest` (repositories against
   an embedded/Testcontainers DB). Import the real collaborator under test (e.g.
   `@Import(SecurityConfig.class)`).
3. **Integration** — `@SpringBootTest` + Testcontainers Postgres for full-context / end-to-end flows.

## ⚠️ `contextLoads` needs a database

`HrmsApplicationTests.contextLoads` is `@SpringBootTest`, so it boots the **full** context including
the datasource. Running `./gradlew test` **fails unless Postgres is reachable** at
`localhost:5432/hrms`. Options:

- Run integration tests only when a DB (or Testcontainers) is available, or
- Adopt Testcontainers so `@SpringBootTest` provisions its own Postgres (preferred once added).

Until then, prefer **slice/unit tests** (no DB) for fast feedback, and run
`./gradlew compileJava compileTestJava` as a zero-DB sanity check.

## What to test for the current foundation (DB-free)

- **`HomeController` / security** — `@WebMvcTest(HomeController.class)` + `@Import(SecurityConfig.class)`:
  `GET /` → 200 + view `index`; unauthenticated protected path → 302 redirect to `/login`;
  `@WithMockUser` protected path → passes security.
- **`GlobalExceptionHandler`** — unit test: instantiate the handler, pass a `MockHttpServletRequest`,
  assert 400/404/500 and body fields (build `MethodArgumentNotValidException` from a
  `BeanPropertyBindingResult` for the validation case).
- **`ApiErrorResponse`** — unit: both `of(...)` factories; `validationErrors` omitted from JSON when null.
- **`ResourceNotFoundException`** — unit: message formatting for both constructors.
- **`SecurityConfig`** — unit: `passwordEncoder()` returns a BCrypt encoder that hashes + matches.

> Status: these are **planned/not yet committed** — the only test in the repo today is the stock
> `contextLoads`. Add the above alongside each new module.

## Conventions

- One behavior per test; Arrange-Act-Assert; descriptive method names.
- Every RBAC matrix cell (✔/—) becomes a positive/negative authorization test (`docs/01-product/permissions-matrix.md` §14).
- Money/attendance logic: cover boundaries, rounding, idempotent-retry, and concurrency where relevant.
- No flaky tests; no reliance on wall-clock/network beyond controlled containers.
