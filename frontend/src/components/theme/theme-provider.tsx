import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { useUiStore } from '@/stores/ui-store'

/**
 * Applies the selected theme to the document root by toggling the `dark` class
 * (which shadcn/Tailwind tokens key off). Honors the OS preference when the
 * theme is "system" and reacts to OS changes live.
 */
export function ThemeProvider({ children }: { children: ReactNode }) {
  const theme = useUiStore((state) => state.theme)

  useEffect(() => {
    const root = document.documentElement
    const media = window.matchMedia('(prefers-color-scheme: dark)')

    const apply = () => {
      const isDark = theme === 'dark' || (theme === 'system' && media.matches)
      root.classList.toggle('dark', isDark)
    }

    apply()
    if (theme === 'system') {
      media.addEventListener('change', apply)
      return () => media.removeEventListener('change', apply)
    }
  }, [theme])

  return <>{children}</>
}
