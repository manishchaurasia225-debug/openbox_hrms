import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { authApi } from './api'
import { tokenStorage } from '@/lib/auth/token-storage'
import type { CurrentUser, LoginRequest } from '@/types/auth'

export const authKeys = {
  me: ['auth', 'me'] as const,
}

/** Loads the authenticated user from /auth/me. Only runs when a session token exists. */
export function useCurrentUser() {
  return useQuery({
    queryKey: authKeys.me,
    queryFn: authApi.me,
    enabled: tokenStorage.hasSession(),
    staleTime: 5 * 60_000,
    retry: false,
  })
}

/** Logs in, persists tokens, and seeds the current-user cache from the response. */
export function useLogin() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: LoginRequest) => authApi.login(body),
    onSuccess: (data) => {
      tokenStorage.setTokens(data.accessToken, data.refreshToken)
      queryClient.setQueryData<CurrentUser>(authKeys.me, data.user)
    },
  })
}

/** Revokes the refresh token server-side, then clears local tokens and all cached data. */
export function useLogout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => authApi.logout(),
    onSettled: () => {
      tokenStorage.clear()
      queryClient.clear()
    },
  })
}
