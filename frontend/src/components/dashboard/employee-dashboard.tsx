import { Link } from 'react-router-dom'
import {
  Bell,
  CalendarDays,
  CalendarOff,
  Megaphone,
  Plus,
  UserCheck,
  UserCircle,
  Upload,
} from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { StatCard } from '@/components/dashboard/stat-card'
import { TiltCard } from '@/components/dashboard/tilt-card'
import { Can } from '@/components/auth/require-permission'
import { useAuth } from '@/lib/auth/use-auth'
import { formatDate } from '@/lib/format'
import { formatWorkingMinutes } from '@/features/attendance/constants'
import { useMyDashboard } from '@/features/dashboard/hooks'

function greeting(): string {
  const hour = new Date().getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 17) return 'Good afternoon'
  return 'Good evening'
}

/** Self-service homepage for a standard employee — their own attendance, leave, holidays, and notices. */
export function EmployeeDashboard() {
  const { user } = useAuth()
  const { data, isLoading } = useMyDashboard()
  const firstName = user?.fullName?.split(' ')[0] ?? 'there'

  const leaveRemaining = (data?.leaveBalances ?? []).reduce((sum, b) => sum + b.remaining, 0)
  const subtitleParts = [data?.employeeCode, data?.department, data?.designation].filter(Boolean)

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">
            {greeting()}, {firstName}
          </h1>
          <p className="text-sm text-muted-foreground">
            {subtitleParts.length ? subtitleParts.join(' · ') : 'Your personal workspace'}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Can anyOf={['ATTENDANCE:CREATE']}>
            <Button asChild size="sm">
              <Link to="/attendance">
                <UserCheck className="mr-2 size-4" /> Attendance
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
                <Upload className="mr-2 size-4" /> Documents
              </Link>
            </Button>
          </Can>
        </div>
      </div>

      {/* Personal KPIs */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="Profile complete"
          value={`${data?.profileCompletionPercent ?? 0}%`}
          icon={UserCircle}
          accent="blue"
          hint="Keep it up to date"
          to={data ? `/employees/${data.employeeId}` : undefined}
          loading={isLoading}
        />
        <StatCard
          label="Present this month"
          value={data?.attendanceThisMonth.presentDays ?? 0}
          icon={UserCheck}
          accent="emerald"
          hint="Days checked in"
          to="/attendance"
          loading={isLoading}
        />
        <StatCard
          label="Leave remaining"
          value={leaveRemaining}
          icon={CalendarDays}
          accent="violet"
          hint="Across all types"
          to="/leave"
          loading={isLoading}
        />
        <StatCard
          label="Notifications"
          value={data?.unreadNotifications ?? 0}
          icon={Bell}
          accent="amber"
          hint="Unread"
          loading={isLoading}
        />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        {/* Leave balances */}
        <TiltCard max={4} className="h-full rounded-xl">
          <Card className="h-full">
            <CardHeader>
              <CardTitle className="text-base">Leave balances</CardTitle>
              <CardDescription>Your allocation for {new Date().getFullYear()}.</CardDescription>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <Skeleton className="h-24 w-full" />
              ) : data && data.leaveBalances.length > 0 ? (
                <ul className="space-y-4">
                  {data.leaveBalances.map((balance) => {
                    const pct = balance.allocated > 0 ? (balance.used / balance.allocated) * 100 : 0
                    return (
                      <li key={balance.id} className="space-y-1.5">
                        <div className="flex items-center justify-between text-sm">
                          <span className="font-medium">{balance.leaveTypeCode}</span>
                          <span className="tabular-nums text-muted-foreground">
                            {balance.remaining} / {balance.allocated} left
                          </span>
                        </div>
                        <div className="h-2 overflow-hidden rounded-full bg-muted">
                          <div
                            className="h-full rounded-full bg-[var(--chart-2)]"
                            style={{ width: `${Math.min(pct, 100)}%` }}
                          />
                        </div>
                      </li>
                    )
                  })}
                </ul>
              ) : (
                <p className="text-sm text-muted-foreground">No leave balances allocated yet.</p>
              )}
            </CardContent>
          </Card>
        </TiltCard>

        {/* Attendance this month */}
        <TiltCard max={4} className="h-full rounded-xl">
          <Card className="h-full">
            <CardHeader>
              <CardTitle className="text-base">This month</CardTitle>
              <CardDescription>Your attendance summary.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-3 gap-3 text-center">
                <MiniStat label="Present" value={data?.attendanceThisMonth.presentDays ?? 0} loading={isLoading} />
                <MiniStat label="On leave" value={data?.attendanceThisMonth.leaveDays ?? 0} loading={isLoading} />
                <MiniStat label="Absent" value={data?.attendanceThisMonth.absentDays ?? 0} loading={isLoading} />
              </div>
              <div className="flex items-center justify-between border-t pt-3 text-sm">
                <span className="text-muted-foreground">Hours worked</span>
                <span className="font-medium tabular-nums">
                  {formatWorkingMinutes(data?.attendanceThisMonth.totalWorkingMinutes)}
                </span>
              </div>
            </CardContent>
          </Card>
        </TiltCard>

        {/* Upcoming holidays */}
        <TiltCard max={4} className="h-full rounded-xl">
          <Card className="h-full">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <CalendarOff className="size-4 text-sky-500" /> Upcoming holidays
              </CardTitle>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <Skeleton className="h-20 w-full" />
              ) : data && data.upcomingHolidays.length > 0 ? (
                <ul className="space-y-3">
                  {data.upcomingHolidays.map((holiday) => (
                    <li key={holiday.id} className="flex items-center justify-between text-sm">
                      <span className="truncate font-medium">{holiday.name}</span>
                      <span className="shrink-0 text-muted-foreground">{formatDate(holiday.holidayDate)}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="text-sm text-muted-foreground">No holidays coming up.</p>
              )}
            </CardContent>
          </Card>
        </TiltCard>
      </div>

      {/* Announcements */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Megaphone className="size-4 text-primary" /> Announcements
          </CardTitle>
          <CardDescription>Latest updates from your organization.</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <Skeleton className="h-16 w-full" />
          ) : data && data.recentAnnouncements.length > 0 ? (
            <ul className="divide-y">
              {data.recentAnnouncements.map((announcement) => (
                <li key={announcement.id} className="space-y-1 py-3 first:pt-0 last:pb-0">
                  <div className="flex items-center gap-2">
                    {announcement.pinned ? (
                      <Badge variant="secondary" className="text-xs">
                        Pinned
                      </Badge>
                    ) : null}
                    <span className="font-medium">{announcement.title}</span>
                    <span className="ml-auto shrink-0 text-xs text-muted-foreground">
                      {formatDate(announcement.publishAt)}
                    </span>
                  </div>
                  <p className="line-clamp-2 text-sm text-muted-foreground">{announcement.body}</p>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-sm text-muted-foreground">No announcements right now.</p>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

function MiniStat({ label, value, loading }: { label: string; value: number; loading?: boolean }) {
  return (
    <div className="space-y-1">
      {loading ? (
        <Skeleton className="mx-auto h-7 w-8" />
      ) : (
        <p className="text-2xl font-semibold tabular-nums">{value}</p>
      )}
      <p className="text-xs text-muted-foreground">{label}</p>
    </div>
  )
}
