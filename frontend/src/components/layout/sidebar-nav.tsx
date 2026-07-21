import { NavLink } from 'react-router-dom'
import { cn } from '@/lib/utils'
import { navGroups } from '@/routes/nav'
import { useAuth } from '@/lib/auth/use-auth'

/** Permission-filtered sidebar navigation, shared by the desktop rail and mobile drawer. */
export function SidebarNav({ onNavigate }: { onNavigate?: () => void }) {
  const { hasAnyAuthority } = useAuth()

  return (
    <nav className="flex flex-col gap-5 px-3 py-4" aria-label="Primary">
      {navGroups.map((group) => {
        const visibleItems = group.items.filter(
          (item) => !item.anyOf || hasAnyAuthority(item.anyOf),
        )
        if (visibleItems.length === 0) return null

        return (
          <div key={group.label} className="space-y-1">
            <p className="px-3 text-xs font-medium uppercase tracking-wider text-muted-foreground/70">
              {group.label}
            </p>
            {visibleItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={onNavigate}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                      : 'text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
                  )
                }
              >
                <item.icon className="size-4 shrink-0" />
                <span className="truncate">{item.label}</span>
              </NavLink>
            ))}
          </div>
        )
      })}
    </nav>
  )
}
