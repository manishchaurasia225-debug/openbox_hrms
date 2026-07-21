import type { LeaveStatus } from './types'
import type { SelectOption } from '@/components/form/select-field'

export const leaveStatusLabels: Record<LeaveStatus, string> = {
  PENDING: 'Pending',
  MANAGER_APPROVED: 'Manager approved',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  CANCELLED: 'Cancelled',
}

export const leaveStatusVariant: Record<
  LeaveStatus,
  'default' | 'secondary' | 'destructive' | 'outline'
> = {
  PENDING: 'secondary',
  MANAGER_APPROVED: 'secondary',
  APPROVED: 'default',
  REJECTED: 'destructive',
  CANCELLED: 'outline',
}

/** Statuses a request can still be approved/rejected from (two-stage workflow). */
export const decidableStatuses: LeaveStatus[] = ['PENDING', 'MANAGER_APPROVED']

/** Statuses the requesting employee can still cancel. */
export const cancellableStatuses: LeaveStatus[] = ['PENDING', 'MANAGER_APPROVED']

/** Status filter options for the management view ("" = all). */
export const leaveStatusFilterOptions: SelectOption[] = [
  { value: 'ALL', label: 'All statuses' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'MANAGER_APPROVED', label: 'Manager approved' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'CANCELLED', label: 'Cancelled' },
]
