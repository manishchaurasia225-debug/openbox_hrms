import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  attendanceApi,
  type AttendanceHistoryParams,
  type AttendanceListParams,
} from './api'
import type { AttendanceCorrectionRequest, CheckInRequest } from './types'

export const attendanceKeys = {
  all: ['attendance'] as const,
  my: (params: AttendanceHistoryParams) => ['attendance', 'my', params] as const,
  list: (params: AttendanceListParams) => ['attendance', 'list', params] as const,
  summary: (employeeId: number, year: number, month: number) =>
    ['attendance', 'summary', employeeId, year, month] as const,
}

export function useMyAttendance(params: AttendanceHistoryParams) {
  return useQuery({
    queryKey: attendanceKeys.my(params),
    queryFn: () => attendanceApi.myHistory(params),
    placeholderData: keepPreviousData,
  })
}

export function useAttendanceRecords(
  params: AttendanceListParams,
  options?: { enabled?: boolean },
) {
  return useQuery({
    queryKey: attendanceKeys.list(params),
    queryFn: () => attendanceApi.list(params),
    placeholderData: keepPreviousData,
    enabled: options?.enabled ?? true,
  })
}

/** Self-service check-in/out and HR corrections/approvals — all invalidate the attendance tree. */
export function useAttendanceMutations() {
  const queryClient = useQueryClient()
  const invalidate = () => queryClient.invalidateQueries({ queryKey: attendanceKeys.all })

  const checkIn = useMutation({
    mutationFn: (body: CheckInRequest) => attendanceApi.checkIn(body),
    onSuccess: invalidate,
  })
  const checkOut = useMutation({
    mutationFn: () => attendanceApi.checkOut(),
    onSuccess: invalidate,
  })
  const correct = useMutation({
    mutationFn: (body: AttendanceCorrectionRequest) => attendanceApi.correct(body),
    onSuccess: invalidate,
  })
  const approve = useMutation({
    mutationFn: (id: number) => attendanceApi.approve(id),
    onSuccess: invalidate,
  })
  const reject = useMutation({
    mutationFn: (id: number) => attendanceApi.reject(id),
    onSuccess: invalidate,
  })

  return { checkIn, checkOut, correct, approve, reject }
}
