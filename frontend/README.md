# OGM HRMS — Frontend

React SPA for the OGM HRMS backend. It follows the Spring Boot API contract exactly (standardized
`ApiResponse` envelope, `PageResponse` pagination, JWT auth) — the backend is the source of truth.

## Stack

- **React 19 + Vite + TypeScript**
- **Tailwind CSS v4 + shadcn/ui** (design system / components)
- **TanStack Query** — all server state
- **React Hook Form + Zod** — forms & validation
- **Axios** — HTTP client with JWT + refresh interceptors
- **React Router v7** — routing
- **Zustand** — client-side UI state only (theme, sidebar)

## Getting started

```bash
npm install
npm run dev        # http://localhost:5173
```

The dev server proxies `/api` to the backend on `http://localhost:8080` (see `vite.config.ts`), so the
Spring Boot app must be running (`../scripts/run-dev.sh`). Log in with the bootstrapped Super Admin.

## Scripts

| Command | Purpose |
|---|---|
| `npm run dev` | Start the dev server (HMR) |
| `npm run build` | Type-check (`tsc -b`) + production build |
| `npm run preview` | Preview the production build |
| `npm run lint` | Lint with oxlint |

## Configuration

`VITE_API_BASE_URL` (see `.env.example`) — the API base path. Defaults to `/api/v1` in dev (proxied);
set it to the deployed API origin in production.

## Structure

```
src/
  config/        env/runtime config
  types/         API + auth types mirroring backend DTOs
  lib/
    api/         axios client (JWT + refresh interceptors) + typed http helpers
    auth/        token storage, RBAC (useAuth), auth-event bridge
    query-client.ts
  features/      feature modules (auth wired; more added per module)
  stores/        Zustand UI store (theme, sidebar)
  components/
    ui/          shadcn/ui primitives
    common/      DataTable, PageHeader, EmptyState, ErrorState, spinner
    layout/      Brand, SidebarNav, UserMenu
    theme/       ThemeProvider, ThemeToggle
    auth/        ProtectedRoute, GuestRoute, RequirePermission, Can
  layouts/       AppLayout (shell), AuthLayout
  routes/        router + nav config (permission-gated)
  pages/         Login, Dashboard, placeholders, 403, 404
```

## Auth & RBAC

Login stores the access + refresh tokens; axios attaches the bearer token and transparently refreshes
on 401. The current user (`/auth/me`) carries `authorities` (backend permission codes) which drive UI
gating via `useAuth().hasAnyAuthority(...)`, `<Can anyOf={[...]}>`, and `<RequirePermission anyOf={[...]}>`
— so what the UI shows matches what the server's `@PreAuthorize` allows.
