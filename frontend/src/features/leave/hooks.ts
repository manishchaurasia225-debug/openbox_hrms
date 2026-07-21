import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { leaveApi, type LeaveListParams } from './api'
import type { ApplyLeaveRequest, LeaveDecisionRequest } from './types'
import type { PageParams } from '@/types/api'

export const leaveKeys = {
  all: ['leave'] as const,
  types: ['leave', 'types'] as const,
  my: (params: PageParams) => ['leave', 'my', params] as const,
  list: (params: LeaveListParams) => ['leave', 'list', params] as const,
}

export function useLeaveTypes() {
  return useQuery({
    queryKey: leaveKeys.types,
    queryFn: leaveApi.leaveTypes,
    staleTime: 5 * 60_000,
  })
}

export function useMyLeaveRequests(params: PageParams) {
  return useQuery({
    queryKey: leaveKeys.my(params),
    queryFn: () => leaveApi.myRequests(params),
    placeholderData: keepPreviousData,
  })
}

export function useLeaveRequests(params: LeaveListParams) {
  return useQuery({
    queryKey: leaveKeys.list(params),
    queryFn: () => leaveApi.listRequests(params),
    placeholderData: keepPreviousData,
  })
}

export function useLeaveMutations() {
  const queryClient = useQueryClient()
  const invalidate = () => queryClient.invalidateQueries({ queryKey: leaveKeys.all })

  const apply = useMutation({
    mutationFn: (body: ApplyLeaveRequest) => leaveApi.apply(body),
    onSuccess: invalidate,
  })
  const approve = useMutation({
    mutationFn: (vars: { id: number; body: LeaveDecisionRequest }) =>
      leaveApi.approve(vars.id, vars.body),
    onSuccess: invalidate,
  })
  const reject = useMutation({
    mutationFn: (vars: { id: number; body: LeaveDecisionRequest }) =>
      leaveApi.reject(vars.id, vars.body),
    onSuccess: invalidate,
  })
  const cancel = useMutation({
    mutationFn: (id: number) => leaveApi.cancel(id),
    onSuccess: invalidate,
  })

  return { apply, approve, reject, cancel }
}
