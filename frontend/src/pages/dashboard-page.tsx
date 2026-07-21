import { CalendarDays, CalendarClock, Users, Wallet } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageHeader } from '@/components/common/page-header'
import { useAuth } from '@/lib/auth/use-auth'

const summaryCards = [
  { label: 'Employees', icon: Users, hint: 'Directory & profiles' },
  { label: 'Attendance', icon: CalendarClock, hint: "Today's check-ins" },
  { label: 'Leave', icon: CalendarDays, hint: 'Pending approvals' },
  { label: 'Payroll', icon: Wallet, hint: 'Current cycle' },
]

/** Landing dashboard. Feature widgets are wired up as their pages are built. */
export function DashboardPage() {
  const { user } = useAuth()
  const firstName = user?.fullName?.split(' ')[0] ?? 'there'

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Welcome back, ${firstName}`}
        description="Here's an overview of your HR operations."
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {summaryCards.map((card) => (
          <Card key={card.label}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {card.label}
              </CardTitle>
              <card.icon className="size-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">{card.hint}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Your access</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm text-muted-foreground">
          <p>
            Signed in as <span className="font-medium text-foreground">{user?.email}</span>
          </p>
          <p>
            Roles: <span className="font-medium text-foreground">{user?.roles.join(', ') || '—'}</span>
          </p>
          <p>{user?.authorities.length ?? 0} permissions granted.</p>
        </CardContent>
      </Card>
    </div>
  )
}
