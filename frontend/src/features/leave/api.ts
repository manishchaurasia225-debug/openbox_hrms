import { http } from '@/lib/api/client'
import type { PageParams, PageResponse } from '@/types/api'
import type {
  ApplyLeaveRequest,
  LeaveDecisionRequest,
  LeaveRequest,
  LeaveStatus,
  LeaveType,
} from './types'

export interface LeaveListParams extends PageParams {
  status?: LeaveStatus
  employeeId?: number
}

/** com.ogm.hrms.controller.LeaveController + LeaveTypeController */
export const leaveApi = {
  leaveTypes: () => http.get<LeaveType[]>('/leave-types'),
  myRequests: (params: PageParams) =>
    http.get<PageResponse<LeaveRequest>>('/leave/requests/me', { params }),
  listRequests: (params: LeaveListParams) =>
    http.get<PageResponse<LeaveRequest>>('/leave/requests', { params }),
  apply: (body: ApplyLeaveRequest) => http.post<LeaveRequest>('/leave/requests', body),
  approve: (id: number, body: LeaveDecisionRequest) =>
    http.post<LeaveRequest>(`/leave/requests/${id}/approve`, body),
  reject: (id: number, body: LeaveDecisionRequest) =>
    http.post<LeaveRequest>(`/leave/requests/${id}/reject`, body),
  cancel: (id: number) => http.post<LeaveRequest>(`/leave/requests/${id}/cancel`),
}
