import { QueryClient } from '@tanstack/react-query'
import { ApiError } from '@/lib/api/client'

/**
 * Shared TanStack Query client. Auth failures (401/403) are never retried — the
 * axios layer already handles token refresh, so a surfaced 401 means the session
 * is truly gone. Other errors get a single retry.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
          return false
        }
        return failureCount < 1
      },
    },
    mutations: {
      retry: false,
    },
  },
})
