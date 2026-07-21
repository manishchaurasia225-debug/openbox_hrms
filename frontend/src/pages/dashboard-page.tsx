import { Link } from 'react-router-dom'
import {
  CalendarClock,
  CalendarDays,
  Cake,
  CheckCircle2,
  FileText,
  PartyPopper,
  Plus,
  ShieldCheck,
  UserPlus,
  Users,
  UserCheck,
} from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { StatCard } from '@/components/dashboard/stat-card'
import { BarList } from '@/components/dashboard/bar-list'
import { TiltCard } from '@/components/dashboard/tilt-card'
import { Can } from '@/components/auth/require-permission'
import { useAuth } from '@/lib/auth/use-auth'
import { formatDate } from '@/lib/format'
import { useHrDashboard } from '@/features/dashboard/hooks'
import type { PersonDate } from '@/features/dashboard/types'

function greeting(): string {
  const hour = new Date().getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 17) return 'Good afternoon'
  return 'Good evening'
}

function initials(name: string): string {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')
}

/** A compact list of people tied to an upcoming date (birthdays / anniversaries). */
function PeopleList({ people, empty }: { people: PersonDate[]; empty: string }) {
  if (people.length === 0) {
    return <p className="py-2 text-sm text-muted-foreground">{empty}</p>
  }
  return (
    <ul className="space-y-3">
      {people.map((person) => (
        <li key={`${person.employeeId}-${person.date}`} className="flex items-center gap-3">
          <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-muted text-xs font-semibold">
            {initials(person.name)}
          </span>
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium">{person.name}</p>
            <p className="text-xs text-muted-foreground">{formatDate(person.date)}</p>
          </div>
        </li>
      ))}
    </ul>
  )
}

export function DashboardPage() {
  const { user, hasAuthority } = useAuth()
  const firstName = user?.fullName?.split(' ')[0] ?? 'there'
  const canSeeHr = hasAuthority('DASHBOARD:VIEW')
  const { data, isLoading } = useHrDashboard(canSeeHr)

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">
            {greeting()}, {firstName}
          </h1>
          <p className="text-sm text-muted-foreground">Here's what's happening across your organization today.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Can anyOf={['EMPLOYEE:CREATE']}>
            <Button asChild size="sm">
              <Link to="/employees/new">
                <UserPlus className="mr-2 size-4" /> Add employee
              </Link>
            </Button>
          </Can>
          <Can anyOf={['LEAVE:CREATE']}>
            <Button asChild size="sm" variant="outline">
              <Link to="/leave">
                <Plus className="mr-2 size-4" /> Apply leave
              </Link>
            </Button>
          </Can>
          <Can anyOf={['DOCUMENT:CREATE']}>
            <Button asChild size="sm" variant="outline">
              <Link to="/documents">
                <FileText className="mr-2 size-4" /> Upload document
              </Link>
            </Button>
          </Can>
        </div>
      </div>

      {/* KPI row */}
      {canSeeHr ? (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <StatCard
              label="Total employees"
              value={data?.totalEmployees ?? 0}
              icon={Users}
              accent="blue"
              hint="Active directory"
              to="/employees"
              loading={isLoading}
            />
            <StatCard
              label="Present today"
              value={data?.presentToday ?? 0}
              icon={UserCheck}
              accent="emerald"
              hint="Checked in"
              to="/attendance"
              loading={isLoading}
            />
            <StatCard
              label="On leave today"
              value={data?.onLeaveToday ?? 0}
              icon={CalendarDays}
              accent="violet"
              hint="Approved leave"
              to="/leave"
              loading={isLoading}
            />
            <StatCard
              label="Pending approvals"
              value={(data?.pendingLeaveApprovals ?? 0) + (data?.pendingWfhApprovals ?? 0)}
              icon={CheckCircle2}
              accent="amber"
              hint="Leave & WFH requests"
              to="/leave"
              loading={isLoading}
            />
          </div>

          {/* Distribution + upcoming */}
          <div className="grid gap-4 lg:grid-cols-3">
            <TiltCard max={4} wrapperClassName="lg:col-span-2" className="h-full rounded-xl">
            <Card className="h-full">
              <CardHeader>
                <CardTitle className="text-base">Workforce distribution</CardTitle>
                <CardDescription>How your people are spread across the organization.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid gap-6 sm:grid-cols-2">
                  <div className="space-y-3">
                    <p className="text-sm font-medium text-muted-foreground">By department</p>
                    {isLoading ? (
                      <Skeleton className="h-24 w-full" />
                    ) : (
                      <BarList entries={data?.departmentDistribution ?? []} />
                    )}
                  </div>
                  <div className="space-y-3">
                    <p className="text-sm font-medium text-muted-foreground">By gender</p>
                    {isLoading ? (
                      <Skeleton className="h-24 w-full" />
                    ) : (
                      <BarList entries={data?.genderDistribution ?? []} />
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-4 border-t pt-4">
                  <MiniStat label="New joiners (30d)" value={data?.newJoinersLast30Days ?? 0} loading={isLoading} />
                  <MiniStat label="Departments" value={data?.totalDepartments ?? 0} loading={isLoading} />
                  <MiniStat label="Pending WFH" value={data?.pendingWfhApprovals ?? 0} loading={isLoading} />
                </div>
              </CardContent>
            </Card>
            </TiltCard>

            <TiltCard max={4} className="h-full rounded-xl">
            <Card className="h-full">
              <CardHeader>
                <CardTitle className="text-base">Upcoming</CardTitle>
                <CardDescription>Birthdays and work anniversaries.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-5">
                <div className="space-y-3">
                  <div className="flex items-center gap-2 text-sm font-medium">
                    <Cake className="size-4 text-rose-500" /> Birthdays
                  </div>
                  {isLoading ? (
                    <Skeleton className="h-12 w-full" />
                  ) : (
                    <PeopleList people={data?.upcomingBirthdays ?? []} empty="No birthdays coming up." />
                  )}
                </div>
                <div className="space-y-3">
                  <div className="flex items-center gap-2 text-sm font-medium">
                    <PartyPopper className="size-4 text-amber-500" /> Anniversaries
                  </div>
                  {isLoading ? (
                    <Skeleton className="h-12 w-full" />
                  ) : (
                    <PeopleList people={data?.upcomingAnniversaries ?? []} empty="No anniversaries coming up." />
                  )}
                </div>
              </CardContent>
            </Card>
            </TiltCard>
          </div>
        </>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <CalendarClock className="size-4" /> Your workspace
            </CardTitle>
            <CardDescription>Quick links to your self-service tools.</CardDescription>
          </CardHeader>
          <CardContent className="grid gap-3 sm:grid-cols-3">
            <Button asChild variant="outline">
              <Link to="/attendance">Attendance</Link>
            </Button>
            <Button asChild variant="outline">
              <Link to="/leave">Leave</Link>
            </Button>
            <Button asChild variant="outline">
              <Link to="/documents">Documents</Link>
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Access summary */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <ShieldCheck className="size-4 text-primary" /> Your access
          </CardTitle>
        </CardHeader>
        <CardContent className="flex flex-wrap items-center gap-x-8 gap-y-3 text-sm">
          <div>
            <p className="text-muted-foreground">Signed in as</p>
            <p className="font-medium">{user?.email}</p>
          </div>
          <div>
            <p className="text-muted-foreground">Roles</p>
            <div className="mt-0.5 flex flex-wrap gap-1">
              {user?.roles.length ? (
                user.roles.map((role) => (
                  <Badge key={role} variant="secondary">
                    {role.replace(/_/g, ' ')}
                  </Badge>
                ))
              ) : (
                <span className="font-medium">—</span>
              )}
            </div>
          </div>
          <div>
            <p className="text-muted-foreground">Permissions</p>
            <p className="font-medium">{user?.authorities.length ?? 0} granted</p>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

function MiniStat({ label, value, loading }: { label: string; value: number; loading?: boolean }) {
  return (
    <div className="space-y-1">
      {loading ? (
        <Skeleton className="h-7 w-10" />
      ) : (
        <p className="text-2xl font-semibold tabular-nums">{value}</p>
      )}
      <p className="text-xs text-muted-foreground">{label}</p>
    </div>
  )
}
