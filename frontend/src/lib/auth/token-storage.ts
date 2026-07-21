/**
 * Persists the JWT access + opaque refresh tokens. The backend returns refresh
 * tokens in the response body (not an httpOnly cookie), so the SPA is responsible
 * for storing them. localStorage keeps the session across reloads; this is an
 * internal HR tool behind auth, an accepted trade-off for that context.
 */
const ACCESS_KEY = 'hrms.accessToken'
const REFRESH_KEY = 'hrms.refreshToken'

export const tokenStorage = {
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_KEY)
  },
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_KEY)
  },
  setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(ACCESS_KEY, accessToken)
    localStorage.setItem(REFRESH_KEY, refreshToken)
  },
  clear(): void {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
  hasSession(): boolean {
    return localStorage.getItem(ACCESS_KEY) != null
  },
}
