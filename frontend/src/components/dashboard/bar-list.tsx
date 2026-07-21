import type { CountEntry } from '@/features/dashboard/types'

const BAR_COLORS = [
  'bg-[var(--chart-1)]',
  'bg-[var(--chart-2)]',
  'bg-[var(--chart-3)]',
  'bg-[var(--chart-4)]',
  'bg-[var(--chart-5)]',
]

/** Horizontal labelled bars for a distribution (department, gender, …). */
export function BarList({ entries }: { entries: CountEntry[] }) {
  if (entries.length === 0) {
    return <p className="text-sm text-muted-foreground">No data yet.</p>
  }
  const max = Math.max(...entries.map((e) => e.count), 1)

  return (
    <ul className="space-y-3">
      {entries.map((entry, index) => (
        <li key={entry.label} className="space-y-1.5">
          <div className="flex items-center justify-between text-sm">
            <span className="truncate font-medium">{entry.label}</span>
            <span className="tabular-nums text-muted-foreground">{entry.count}</span>
          </div>
          <div className="h-2 overflow-hidden rounded-full bg-muted">
            <div
              className={`h-full rounded-full ${BAR_COLORS[index % BAR_COLORS.length]}`}
              style={{ width: `${Math.max((entry.count / max) * 100, 4)}%` }}
            />
          </div>
        </li>
      ))}
    </ul>
  )
}
