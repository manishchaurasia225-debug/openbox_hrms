import { Loader2 } from 'lucide-react'

/** Centered loading indicator for full-page/route-level suspense states. */
export function FullPageSpinner({ label = 'Loading…' }: { label?: string }) {
  return (
    <div
      className="flex min-h-svh w-full items-center justify-center"
      role="status"
      aria-live="polite"
    >
      <div className="flex flex-col items-center gap-3 text-muted-foreground">
        <Loader2 className="size-6 animate-spin" />
        <span className="text-sm">{label}</span>
      </div>
    </div>
  )
}
