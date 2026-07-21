import { Outlet } from 'react-router-dom'
import { brandIcon as BrandIcon } from '@/routes/nav'
import { env } from '@/config/env'
import { ThemeToggle } from '@/components/theme/theme-toggle'

/** Centered, minimal layout for unauthenticated pages (login, password reset). */
export function AuthLayout() {
  return (
    <div className="relative flex min-h-svh flex-col items-center justify-center bg-muted/30 p-4">
      <div className="absolute right-4 top-4">
        <ThemeToggle />
      </div>
      <div className="mb-6 flex items-center gap-2">
        <span className="flex size-9 items-center justify-center rounded-md bg-primary text-primary-foreground">
          <BrandIcon className="size-5" />
        </span>
        <span className="text-lg font-semibold tracking-tight">{env.appName}</span>
      </div>
      <div className="w-full max-w-sm">
        <Outlet />
      </div>
      <p className="mt-8 text-xs text-muted-foreground">Enterprise HR Operations Platform</p>
    </div>
  )
}
