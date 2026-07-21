import type { ReactNode } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '@/lib/auth/use-auth'

/**
 * Route guard: renders the nested routes only if the user holds at least one of
 * the required authorities, otherwise redirects to the 403 page. Use as a
 * layout route with an `anyOf` list of permission codes.
 */
export function RequirePermission({ anyOf }: { anyOf: string[] }) {
  const { hasAnyAuthority } = useAuth()
  if (!hasAnyAuthority(anyOf)) {
    return <Navigate to="/403" replace />
  }
  return <Outlet />
}

/**
 * Inline RBAC gate for conditional UI (buttons, menu items, columns). Renders
 * children only when the user holds one of `anyOf`; otherwise renders `fallback`.
 */
export function Can({
  anyOf,
  children,
  fallback = null,
}: {
  anyOf: string[]
  children: ReactNode
  fallback?: ReactNode
}) {
  const { hasAnyAuthority } = useAuth()
  return <>{hasAnyAuthority(anyOf) ? children : fallback}</>
}
