import type { AttendanceType, ApprovalStatus } from './types'
import type { SelectOption } from '@/components/form/select-field'

type BadgeVariant = 'default' | 'secondary' | 'destructive' | 'outline'

export const attendanceTypeLabels: Record<AttendanceType, string> = {
  OFFICE: 'Office',
  WORK_FROM_HOME: 'Work from home',
  CLIENT_VISIT: 'Client visit',
  BUSINESS_TRAVEL: 'Business travel',
  CASUAL_LEAVE: 'Casual leave',
  SICK_LEAVE: 'Sick leave',
  EARNED_LEAVE: 'Earned leave',
  HALF_DAY: 'Half day',
  EARLY_DEPARTURE: 'Early departure',
  COMP_OFF: 'Comp off',
  HOLIDAY: 'Holiday',
  WEEKEND: 'Weekend',
  ABSENT: 'Absent',
}

export const attendanceTypeVariant: Record<AttendanceType, BadgeVariant> = {
  OFFICE: 'default',
  WORK_FROM_HOME: 'secondary',
  CLIENT_VISIT: 'secondary',
  BUSINESS_TRAVEL: 'secondary',
  CASUAL_LEAVE: 'outline',
  SICK_LEAVE: 'outline',
  EARNED_LEAVE: 'outline',
  HALF_DAY: 'outline',
  EARLY_DEPARTURE: 'outline',
  COMP_OFF: 'outline',
  HOLIDAY: 'outline',
  WEEKEND: 'outline',
  ABSENT: 'destructive',
}

export const approvalStatusLabels: Record<ApprovalStatus, string> = {
  NOT_REQUIRED: 'Not required',
  PENDING: 'Pending',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
}

export const approvalStatusVariant: Record<ApprovalStatus, BadgeVariant> = {
  NOT_REQUIRED: 'outline',
  PENDING: 'secondary',
  APPROVED: 'default',
  REJECTED: 'destructive',
}

/** Only PENDING records can be approved/rejected. */
export const decidableApprovalStatuses: ApprovalStatus[] = ['PENDING']

/** Attendance types offered as options in the correction/regularization form. */
export const attendanceTypeOptions: SelectOption[] = (
  Object.keys(attendanceTypeLabels) as AttendanceType[]
).map((type) => ({ value: type, label: attendanceTypeLabels[type] }))

/** Check-in mode options for the self-service check-in dialog. */
export const checkInModeOptions: SelectOption[] = [
  { value: 'OFFICE', label: 'Office' },
  { value: 'WORK_FROM_HOME', label: 'Work from home' },
]

/** Format a duration in minutes as "Xh Ym" (or an em dash when absent). */
export function formatWorkingMinutes(minutes?: number | null): string {
  if (minutes == null) return '—'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) return `${mins}m`
  if (mins === 0) return `${hours}h`
  return `${hours}h ${mins}m`
}
