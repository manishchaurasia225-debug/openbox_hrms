import type { DocumentType } from './types'
import type { SelectOption } from '@/components/form/select-field'

export const documentTypeLabels: Record<DocumentType, string> = {
  RESUME: 'Resume',
  OFFER_LETTER: 'Offer letter',
  JOINING_LETTER: 'Joining letter',
  EXPERIENCE_LETTER: 'Experience letter',
  SALARY_SLIP: 'Salary slip',
  COMPANY_POLICY: 'Company policy',
}

export const documentTypeVariant: Record<
  DocumentType,
  'default' | 'secondary' | 'destructive' | 'outline'
> = {
  RESUME: 'secondary',
  OFFER_LETTER: 'default',
  JOINING_LETTER: 'default',
  EXPERIENCE_LETTER: 'secondary',
  SALARY_SLIP: 'outline',
  COMPANY_POLICY: 'outline',
}

/** Document type options for the upload form. */
export const documentTypeOptions: SelectOption[] = (
  Object.keys(documentTypeLabels) as DocumentType[]
).map((type) => ({ value: type, label: documentTypeLabels[type] }))

/** Format a byte count as a human-readable size (e.g. "1.2 MB"). */
export function formatFileSize(bytes?: number | null): string {
  if (bytes == null) return '—'
  if (bytes < 1024) return `${bytes} B`
  const units = ['KB', 'MB', 'GB']
  let value = bytes / 1024
  let unit = 0
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024
    unit++
  }
  return `${value.toFixed(1)} ${units[unit]}`
}
