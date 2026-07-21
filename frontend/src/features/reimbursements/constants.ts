import type { ExpenseCategory, ReimbursementStatus } from './types'
import type { SelectOption } from '@/components/form/select-field'

export const reimbursementStatusLabels: Record<ReimbursementStatus, string> = {
  SUBMITTED: 'Submitted',
  MANAGER_APPROVED: 'Manager approved',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  PAID: 'Paid',
  CANCELLED: 'Cancelled',
}

export const reimbursementStatusVariant: Record<
  ReimbursementStatus,
  'default' | 'secondary' | 'destructive' | 'outline'
> = {
  SUBMITTED: 'secondary',
  MANAGER_APPROVED: 'secondary',
  APPROVED: 'default',
  REJECTED: 'destructive',
  PAID: 'default',
  CANCELLED: 'outline',
}

export const expenseCategoryLabels: Record<ExpenseCategory, string> = {
  TRAVEL: 'Travel',
  FUEL: 'Fuel',
  FOOD: 'Food',
  INTERNET: 'Internet',
  MEDICAL: 'Medical',
  OTHER: 'Other',
}

export const expenseCategoryOptions: SelectOption[] = (
  Object.keys(expenseCategoryLabels) as ExpenseCategory[]
).map((value) => ({ value, label: expenseCategoryLabels[value] }))

/** Statuses a reviewer can still approve/reject. */
export const decidableStatuses: ReimbursementStatus[] = ['SUBMITTED', 'MANAGER_APPROVED']
/** Approved claims can be marked paid. */
export const payableStatuses: ReimbursementStatus[] = ['APPROVED']
/** The submitter can still cancel these. */
export const cancellableStatuses: ReimbursementStatus[] = ['SUBMITTED', 'MANAGER_APPROVED']

export const reimbursementStatusFilterOptions: SelectOption[] = [
  { value: 'ALL', label: 'All statuses' },
  { value: 'SUBMITTED', label: 'Submitted' },
  { value: 'MANAGER_APPROVED', label: 'Manager approved' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'PAID', label: 'Paid' },
  { value: 'CANCELLED', label: 'Cancelled' },
]
