import { http } from '@/lib/api/client'
import type { PageParams, PageResponse } from '@/types/api'
import type {
  AttendanceCorrectionRequest,
  AttendanceRecord,
  AttendanceSummary,
  CheckInRequest,
} from './types'

/** Date-range params shared by the history and management list endpoints (ISO "yyyy-MM-dd"). */
export interface AttendanceHistoryParams extends PageParams {
  from?: string
  to?: string
}

export interface AttendanceListParams extends AttendanceHistoryParams {
  employeeId?: number
  date?: string
}

/** com.ogm.hrms.controller.AttendanceController */
export const attendanceApi = {
  checkIn: (body: CheckInRequest) => http.post<AttendanceRecord>('/attendance/check-in', body),
  checkOut: () => http.post<AttendanceRecord>('/attendance/check-out'),
  myHistory: (params: AttendanceHistoryParams) =>
    http.get<PageResponse<AttendanceRecord>>('/attendance/me', { params }),
  list: (params: AttendanceListParams) =>
    http.get<PageResponse<AttendanceRecord>>('/attendance', { params }),
  correct: (body: AttendanceCorrectionRequest) =>
    http.post<AttendanceRecord>('/attendance/corrections', body),
  approve: (id: number) => http.post<AttendanceRecord>(`/attendance/${id}/approve`),
  reject: (id: number) => http.post<AttendanceRecord>(`/attendance/${id}/reject`),
  summary: (employeeId: number, year: number, month: number) =>
    http.get<AttendanceSummary>('/attendance/summary', { params: { employeeId, year, month } }),
}
