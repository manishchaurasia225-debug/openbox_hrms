import { useCurrentUser } from '@/features/auth/hooks'

/**
 * Convenience view over the authenticated user for RBAC gating in the UI.
 * Authorities are the backend permission codes ("MODULE:ACTION") carried on the
 * user — the same values checked by @PreAuthorize server-side, so UI visibility
 * matches server enforcement.
 */
export function useAuth() {
  const { data: user, isLoading, isError } = useCurrentUser()
  const authorities = user?.authorities ?? []
  const roles = user?.roles ?? []

  return {
    user,
    isLoading,
    isError,
    isAuthenticated: !!user,
    hasAuthority: (code: string) => authorities.includes(code),
    hasAnyAuthority: (codes: string[]) => codes.some((code) => authorities.includes(code)),
    hasRole: (role: string) => roles.includes(role),
  }
}
