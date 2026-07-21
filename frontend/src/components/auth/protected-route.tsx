import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { tokenStorage } from '@/lib/auth/token-storage'
import { useCurrentUser } from '@/features/auth/hooks'
import { FullPageSpinner } from '@/components/common/full-page-spinner'

/**
 * Gate for authenticated routes. Redirects to /login when there is no session,
 * shows a spinner while the current user loads, and bounces back to /login if
 * the session token turns out to be invalid.
 */
export function ProtectedRoute() {
  const location = useLocation()
  const { isLoading, isError } = useCurrentUser()

  if (!tokenStorage.hasSession()) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }
  if (isLoading) {
    return <FullPageSpinner />
  }
  if (isError) {
    tokenStorage.clear()
    return <Navigate to="/login" state={{ from: location }} replace />
  }
  return <Outlet />
}
