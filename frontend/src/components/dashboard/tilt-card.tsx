import { useCallback, useRef, type ReactNode, type MouseEvent } from 'react'
import { cn } from '@/lib/utils'

/**
 * Wraps content in a card that tilts toward the cursor in 3D (perspective + rotateX/rotateY)
 * and lifts on hover. Children marked with the `tilt-layer` / `tilt-layer-sm` classes float
 * above the surface for a layered, parallax feel. Pure CSS transforms — no dependencies.
 */
export function TiltCard({
  children,
  className,
  wrapperClassName,
  max = 8,
}: {
  children: ReactNode
  /** Classes for the tilting inner element. */
  className?: string
  /** Classes for the outer perspective wrapper (e.g. grid column spans). */
  wrapperClassName?: string
  /** Maximum rotation in degrees on each axis. */
  max?: number
}) {
  const ref = useRef<HTMLDivElement>(null)

  const handleMove = useCallback(
    (event: MouseEvent<HTMLDivElement>) => {
      const el = ref.current
      if (!el) return
      const rect = el.getBoundingClientRect()
      const px = (event.clientX - rect.left) / rect.width
      const py = (event.clientY - rect.top) / rect.height
      el.style.setProperty('--ry', `${(px - 0.5) * max * 2}deg`)
      el.style.setProperty('--rx', `${(0.5 - py) * max * 2}deg`)
      el.style.setProperty('--mx', `${px * 100}%`)
      el.style.setProperty('--my', `${py * 100}%`)
    },
    [max],
  )

  const reset = useCallback(() => {
    const el = ref.current
    if (!el) return
    el.style.setProperty('--rx', '0deg')
    el.style.setProperty('--ry', '0deg')
  }, [])

  return (
    <div className={cn('tilt-perspective', wrapperClassName)}>
      <div ref={ref} onMouseMove={handleMove} onMouseLeave={reset} className={cn('tilt-card', className)}>
        {children}
      </div>
    </div>
  )
}
