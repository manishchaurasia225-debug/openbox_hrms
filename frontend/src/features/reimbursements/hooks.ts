import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { reimbursementsApi, type ReimbursementListParams } from './api'
import type { ReimbursementDecisionRequest, SubmitReimbursementRequest } from './types'
import type { PageParams } from '@/types/api'

export const reimbursementKeys = {
  all: ['reimbursements'] as const,
  my: (params: PageParams) => ['reimbursements', 'my', params] as const,
  list: (params: ReimbursementListParams) => ['reimbursements', 'list', params] as const,
}

export function useMyReimbursements(params: PageParams) {
  return useQuery({
    queryKey: reimbursementKeys.my(params),
    queryFn: () => reimbursementsApi.myClaims(params),
    placeholderData: keepPreviousData,
  })
}

export function useReimbursements(params: ReimbursementListParams) {
  return useQuery({
    queryKey: reimbursementKeys.list(params),
    queryFn: () => reimbursementsApi.list(params),
    placeholderData: keepPreviousData,
  })
}

export function useReimbursementMutations() {
  const queryClient = useQueryClient()
  const invalidate = () => queryClient.invalidateQueries({ queryKey: reimbursementKeys.all })

  const submit = useMutation({
    mutationFn: (body: SubmitReimbursementRequest) => reimbursementsApi.submit(body),
    onSuccess: invalidate,
  })
  const approve = useMutation({
    mutationFn: (vars: { id: number; body: ReimbursementDecisionRequest }) =>
      reimbursementsApi.approve(vars.id, vars.body),
    onSuccess: invalidate,
  })
  const reject = useMutation({
    mutationFn: (vars: { id: number; body: ReimbursementDecisionRequest }) =>
      reimbursementsApi.reject(vars.id, vars.body),
    onSuccess: invalidate,
  })
  const pay = useMutation({
    mutationFn: (id: number) => reimbursementsApi.pay(id),
    onSuccess: invalidate,
  })
  const cancel = useMutation({
    mutationFn: (id: number) => reimbursementsApi.cancel(id),
    onSuccess: invalidate,
  })

  return { submit, approve, reject, pay, cancel }
}
