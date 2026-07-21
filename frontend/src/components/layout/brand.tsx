import { Link } from 'react-router-dom'
import { brandIcon as BrandIcon } from '@/routes/nav'
import { env } from '@/config/env'

/** App wordmark used at the top of the sidebar / mobile drawer. */
export function Brand() {
  return (
    <Link to="/dashboard" className="flex h-16 items-center gap-2 border-b px-5">
      <span className="flex size-8 items-center justify-center rounded-md bg-primary text-primary-foreground">
        <BrandIcon className="size-5" />
      </span>
      <span className="text-base font-semibold tracking-tight">{env.appName}</span>
    </Link>
  )
}
