import axios, {
  AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from 'axios'
import { env } from '@/config/env'
import { tokenStorage } from '@/lib/auth/token-storage'
import { emitUnauthorized } from '@/lib/auth/auth-events'
import type { ApiResponse } from '@/types/api'
import type { TokenResponse } from '@/types/auth'

/** Normalized error surfaced to callers/UI — carries the backend message + field errors. */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Array<{ field: string; message: string }>
  readonly requestId?: string

  constructor(
    message: string,
    status: number,
    fieldErrors: Array<{ field: string; message: string }> = [],
    requestId?: string,
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
    this.requestId = requestId
  }
}

/** Retry bookkeeping flag added to the original request config after a refresh. */
type RetriableConfig = InternalAxiosRequestConfig & { _retry?: boolean }

export const apiClient: AxiosInstance = axios.create({
  baseURL: env.apiBaseUrl,
  headers: { 'Content-Type': 'application/json' },
})

// A bare client (no interceptors) used only for the refresh call, to avoid recursion.
const refreshClient = axios.create({ baseURL: env.apiBaseUrl })

// --- Request: attach the bearer token -------------------------------------------------
apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// --- Single-flight token refresh ------------------------------------------------------
let refreshPromise: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const refreshToken = tokenStorage.getRefreshToken()
  if (!refreshToken) throw new Error('No refresh token')
  const response = await refreshClient.post<ApiResponse<TokenResponse>>('/auth/refresh', {
    refreshToken,
  })
  const data = response.data.data
  tokenStorage.setTokens(data.accessToken, data.refreshToken)
  return data.accessToken
}

function isAuthEndpoint(url?: string): boolean {
  return !!url && url.includes('/auth/')
}

function toApiError(error: AxiosError<ApiResponse<unknown>>): ApiError {
  const status = error.response?.status ?? 0
  const body = error.response?.data
  const message =
    body?.message ??
    (status === 0 ? 'Network error — the server is unreachable.' : 'Something went wrong.')
  return new ApiError(message, status, body?.errors ?? [], body?.requestId)
}

// --- Response: refresh on 401, then normalize errors ----------------------------------
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const original = error.config as RetriableConfig | undefined
    const status = error.response?.status

    if (
      status === 401 &&
      original &&
      !original._retry &&
      !isAuthEndpoint(original.url) &&
      tokenStorage.getRefreshToken()
    ) {
      original._retry = true
      try {
        refreshPromise = refreshPromise ?? refreshAccessToken()
        const newToken = await refreshPromise
        refreshPromise = null
        original.headers.Authorization = `Bearer ${newToken}`
        return apiClient(original)
      } catch {
        refreshPromise = null
        tokenStorage.clear()
        emitUnauthorized()
      }
    }

    return Promise.reject(toApiError(error))
  },
)

// --- Thin typed helpers that unwrap the ApiResponse envelope --------------------------
async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise
  return response.data.data
}

export const http = {
  get: <T>(url: string, config?: AxiosRequestConfig) =>
    unwrap<T>(apiClient.get<ApiResponse<T>>(url, config)),
  post: <T>(url: string, body?: unknown, config?: AxiosRequestConfig) =>
    unwrap<T>(apiClient.post<ApiResponse<T>>(url, body, config)),
  put: <T>(url: string, body?: unknown, config?: AxiosRequestConfig) =>
    unwrap<T>(apiClient.put<ApiResponse<T>>(url, body, config)),
  patch: <T>(url: string, body?: unknown, config?: AxiosRequestConfig) =>
    unwrap<T>(apiClient.patch<ApiResponse<T>>(url, body, config)),
  delete: <T>(url: string, config?: AxiosRequestConfig) =>
    unwrap<T>(apiClient.delete<ApiResponse<T>>(url, config)),
}
