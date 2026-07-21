import type { EmploymentStatus, Gender, MaritalStatus } from './types'
import type { SelectOption } from '@/components/form/select-field'

/** Human labels for enum values (used in tables, detail views, and selects). */
export const genderLabels: Record<Gender, string> = {
  MALE: 'Male',
  FEMALE: 'Female',
  OTHER: 'Other',
  UNDISCLOSED: 'Undisclosed',
}

export const maritalStatusLabels: Record<MaritalStatus, string> = {
  SINGLE: 'Single',
  MARRIED: 'Married',
  DIVORCED: 'Divorced',
  WIDOWED: 'Widowed',
  OTHER: 'Other',
}

export const employmentStatusLabels: Record<EmploymentStatus, string> = {
  ACTIVE: 'Active',
  ON_NOTICE: 'On notice',
  RESIGNED: 'Resigned',
  TERMINATED: 'Terminated',
  RETIRED: 'Retired',
}

/** Badge tone per employment status. */
export const employmentStatusVariant: Record<
  EmploymentStatus,
  'default' | 'secondary' | 'destructive' | 'outline'
> = {
  ACTIVE: 'default',
  ON_NOTICE: 'secondary',
  RESIGNED: 'outline',
  TERMINATED: 'destructive',
  RETIRED: 'outline',
}

function toOptions<T extends string>(labels: Record<T, string>): SelectOption[] {
  return (Object.keys(labels) as T[]).map((value) => ({ value, label: labels[value] }))
}

export const genderOptions = toOptions(genderLabels)
export const maritalStatusOptions = toOptions(maritalStatusLabels)
export const employmentStatusOptions = toOptions(employmentStatusLabels)
