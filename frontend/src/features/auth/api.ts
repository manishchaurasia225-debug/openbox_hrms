import { http } from '@/lib/api/client'
import { tokenStorage } from '@/lib/auth/token-storage'
import type {
  CurrentUser,
  EmailRequest,
  LoginRequest,
  PasswordResetConfirmRequest,
  TokenResponse,
} from '@/types/auth'

/**
 * Auth endpoints (com.ogm.hrms.controller.AuthController + AccountController).
 * Paths are relative to the configured API base (/api/v1).
 */
export const authApi = {
  login: (body: LoginRequest) => http.post<TokenResponse>('/auth/login', body),

  logout: () =>
    http.post<void>('/auth/logout', { refreshToken: tokenStorage.getRefreshToken() }),

  me: () => http.get<CurrentUser>('/auth/me'),

  requestPasswordReset: (body: EmailRequest) =>
    http.post<void>('/auth/password-reset/request', body),

  confirmPasswordReset: (body: PasswordResetConfirmRequest) =>
    http.post<void>('/auth/password-reset/confirm', body),
}
