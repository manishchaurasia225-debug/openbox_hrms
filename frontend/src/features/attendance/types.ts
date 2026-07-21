/** Types mirroring com.ogm.hrms.dto.attendance.* — do not diverge from the backend. */

/** com.ogm.hrms.enums.AttendanceType */
export type AttendanceType =
  | 'OFFICE'
  | 'WORK_FROM_HOME'
  | 'CLIENT_VISIT'
  | 'BUSINESS_TRAVEL'
  | 'CASUAL_LEAVE'
  | 'SICK_LEAVE'
  | 'EARNED_LEAVE'
  | 'HALF_DAY'
  | 'EARLY_DEPARTURE'
  | 'COMP_OFF'
  | 'HOLIDAY'
  | 'WEEKEND'
  | 'ABSENT'

/** com.ogm.hrms.enums.AttendanceSource */
export type AttendanceSource = 'WIFI_IP' | 'MANUAL' | 'CORRECTION' | 'SYSTEM' | 'BIOMETRIC'

/** com.ogm.hrms.enums.ApprovalStatus */
export type ApprovalStatus = 'NOT_REQUIRED' | 'PENDING' | 'APPROVED' | 'REJECTED'

/** com.ogm.hrms.dto.attendance.CheckInRequest.Mode */
export type CheckInMode = 'OFFICE' | 'WORK_FROM_HOME'

/** com.ogm.hrms.dto.attendance.AttendanceResponse */
export interface AttendanceRecord {
  id: number
  employeeId: number
  employeeName: string
  attendanceDate: string
  attendanceType: AttendanceType
  source: AttendanceSource
  clockIn?: string
  clockOut?: string
  workingMinutes?: number
  late: boolean
  halfDay: boolean
  ipAddress?: string
  wfhReason?: string
  workLocation?: string
  approvalStatus: ApprovalStatus
  remarks?: string
}

/** com.ogm.hrms.dto.attendance.CheckInRequest */
export interface CheckInRequest {
  mode: CheckInMode
  wfhReason?: string
  workLocation?: string
  expectedHours?: number
}

/** com.ogm.hrms.dto.attendance.AttendanceCorrectionRequest */
export interface AttendanceCorrectionRequest {
  employeeId: number
  date: string
  attendanceType: AttendanceType
  clockIn?: string
  clockOut?: string
  remarks?: string
}

/** com.ogm.hrms.dto.attendance.AttendanceSummaryResponse */
export interface AttendanceSummary {
  employeeId: number
  year: number
  month: number
  presentDays: number
  leaveDays: number
  absentDays: number
  totalDays: number
  totalWorkingMinutes: number
  countsByType: Partial<Record<AttendanceType, number>>
}
