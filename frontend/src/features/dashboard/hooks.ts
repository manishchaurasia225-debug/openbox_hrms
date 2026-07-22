import { useQuery } from '@tanstack/react-query'
import { dashboardApi } from './api'

export function useHrDashboard(enabled = true) {
  return useQuery({
    queryKey: ['dashboard', 'hr'],
    queryFn: dashboardApi.hr,
    enabled,
    staleTime: 60_000,
  })
}

export function useMyDashboard(enabled = true) {
  return useQuery({
    queryKey: ['dashboard', 'me'],
    queryFn: dashboardApi.me,
    enabled,
    staleTime: 60_000,
  })
}
