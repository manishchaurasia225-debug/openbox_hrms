import { format, parseISO } from 'date-fns'

/** Format a backend date (LocalDate "yyyy-MM-dd" or ISO datetime) for display. */
export function formatDate(value?: string | null): string {
  if (!value) return '—'
  try {
    return format(parseISO(value), 'dd MMM yyyy')
  } catch {
    return value
  }
}

/** Format an ISO datetime (OffsetDateTime) for display. */
export function formatDateTime(value?: string | null): string {
  if (!value) return '—'
  try {
    return format(parseISO(value), 'dd MMM yyyy, HH:mm')
  } catch {
    return value
  }
}

/** Non-empty text or an em dash placeholder. */
export function orDash(value?: string | number | null): string {
  if (value == null || value === '') return '—'
  return String(value)
}

/** Format a numeric amount with grouping (currency-neutral). */
export function formatMoney(value?: number | null): string {
  if (value == null) return '—'
  return new Intl.NumberFormat('en-IN', { maximumFractionDigits: 2 }).format(value)
}
