import { http } from '@/lib/api/client'
import type { PageParams, PageResponse } from '@/types/api'
import type {
  Reimbursement,
  ReimbursementDecisionRequest,
  ReimbursementStatus,
  SubmitReimbursementRequest,
} from './types'

export interface ReimbursementListParams extends PageParams {
  status?: ReimbursementStatus
  employeeId?: number
}

/** com.ogm.hrms.controller.ReimbursementController — /api/v1/reimbursements */
export const reimbursementsApi = {
  myClaims: (params: PageParams) =>
    http.get<PageResponse<Reimbursement>>('/reimbursements/me', { params }),
  list: (params: ReimbursementListParams) =>
    http.get<PageResponse<Reimbursement>>('/reimbursements', { params }),
  submit: (body: SubmitReimbursementRequest) =>
    http.post<Reimbursement>('/reimbursements', body),
  approve: (id: number, body: ReimbursementDecisionRequest) =>
    http.post<Reimbursement>(`/reimbursements/${id}/approve`, body),
  reject: (id: number, body: ReimbursementDecisionRequest) =>
    http.post<Reimbursement>(`/reimbursements/${id}/reject`, body),
  pay: (id: number) => http.post<Reimbursement>(`/reimbursements/${id}/pay`),
  cancel: (id: number) => http.post<Reimbursement>(`/reimbursements/${id}/cancel`),
}
