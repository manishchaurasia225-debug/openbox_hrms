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
