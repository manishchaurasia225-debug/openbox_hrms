import { Navigate, Outlet } from 'react-router-dom'
import { tokenStorage } from '@/lib/auth/token-storage'

/** Keeps already-authenticated users out of the login/auth pages. */
export function GuestRoute() {
  if (tokenStorage.hasSession()) {
    return <Navigate to="/dashboard" replace />
  }
  return <Outlet />
}
