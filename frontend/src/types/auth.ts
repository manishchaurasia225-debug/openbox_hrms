/**
 * Auth/identity types mirroring the backend DTOs in com.ogm.hrms.dto.auth
 * (CurrentUserResponse, TokenResponse, LoginRequest, ...). Do not invent fields.
 */

/** com.ogm.hrms.dto.auth.CurrentUserResponse */
export interface CurrentUser {
  id: number
  email: string
  fullName: string
  /** Role names, e.g. "SUPER_ADMIN". */
  roles: string[]
  /** Flattened permission codes ("MODULE:ACTION") + role authorities — used for UI RBAC gating. */
  authorities: string[]
}

/** com.ogm.hrms.dto.auth.TokenResponse — returned by POST /auth/login and /auth/refresh. */
export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
  user: CurrentUser
}

/** com.ogm.hrms.dto.auth.LoginRequest */
export interface LoginRequest {
  email: string
  password: string
}

/** com.ogm.hrms.dto.auth.PasswordResetRequest / EmailAddressRequest */
export interface EmailRequest {
  email: string
}

/** com.ogm.hrms.dto.auth.PasswordResetConfirmRequest */
export interface PasswordResetConfirmRequest {
  token: string
  newPassword: string
}
