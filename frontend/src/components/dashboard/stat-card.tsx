import type { ComponentType } from 'react'
import { Link } from 'react-router-dom'
import type { LucideProps } from 'lucide-react'
import { ArrowUpRight } from 'lucide-react'
import { Card } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { TiltCard } from '@/components/dashboard/tilt-card'
import { cn } from '@/lib/utils'

export type StatAccent = 'blue' | 'emerald' | 'amber' | 'violet' | 'rose' | 'sky'

/** Full literal class strings per accent so Tailwind keeps them (no dynamic interpolation). */
const accentChip: Record<StatAccent, string> = {
  blue: 'bg-blue-500/10 text-blue-600 dark:text-blue-400',
  emerald: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400',
  amber: 'bg-amber-500/10 text-amber-600 dark:text-amber-400',
  violet: 'bg-violet-500/10 text-violet-600 dark:text-violet-400',
  rose: 'bg-rose-500/10 text-rose-600 dark:text-rose-400',
  sky: 'bg-sky-500/10 text-sky-600 dark:text-sky-400',
}

interface StatCardProps {
  label: string
  value: number | string
  icon: ComponentType<LucideProps>
  hint?: string
  accent?: StatAccent
  to?: string
  loading?: boolean
}

export function StatCard({
  label,
  value,
  icon: Icon,
  hint,
  accent = 'blue',
  to,
  loading,
}: StatCardProps) {
  const inner = (
    <TiltCard className="h-full rounded-xl">
      <Card className="relative h-full overflow-hidden p-5">
        {/* cursor-following glossy highlight */}
        <span className="tilt-sheen" aria-hidden />

        <div className="relative flex items-start justify-between gap-3">
          <div className="tilt-layer-sm min-w-0 space-y-1">
            <p className="text-sm font-medium text-muted-foreground">{label}</p>
            {loading ? (
              <Skeleton className="h-9 w-16" />
            ) : (
              <p className="text-3xl font-semibold tracking-tight tabular-nums">{value}</p>
            )}
            {hint ? <p className="truncate text-xs text-muted-foreground">{hint}</p> : null}
          </div>
          <span
            className={cn(
              'tilt-layer flex size-11 shrink-0 items-center justify-center rounded-xl shadow-sm',
              accentChip[accent],
            )}
          >
            <Icon className="size-5" />
          </span>
        </div>

        {to ? (
          <ArrowUpRight className="absolute right-4 bottom-4 size-4 text-muted-foreground/40" />
        ) : null}
      </Card>
    </TiltCard>
  )

  return to ? (
    <Link to={to} className="block h-full">
      {inner}
    </Link>
  ) : (
    inner
  )
}
