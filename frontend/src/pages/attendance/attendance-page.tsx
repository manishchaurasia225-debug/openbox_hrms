import { useState } from 'react'
import { Check, Filter, LogIn, LogOut, MoreHorizontal, Pencil, UserX, X } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { PageHeader } from '@/components/common/page-header'
import { ErrorState } from '@/components/common/error-state'
import { EmptyState } from '@/components/common/empty-state'
import { DataTable, type DataTableColumn } from '@/components/common/data-table'
import { Can } from '@/components/auth/require-permission'
import { AttendanceTypeBadge } from '@/components/attendance/attendance-type-badge'
import { ApprovalStatusBadge } from '@/components/attendance/approval-status-badge'
import { CheckInDialog } from '@/components/attendance/check-in-dialog'
import { AttendanceCorrectionDialog } from '@/components/attendance/attendance-correction-dialog'
import { useAuth } from '@/lib/auth/use-auth'
import { ApiError } from '@/lib/api/client'
import { formatDate, formatDateTime } from '@/lib/format'
import {
  useAttendanceMutations,
  useAttendanceRecords,
  useMyAttendance,
} from '@/features/attendance/hooks'
import { decidableApprovalStatuses, formatWorkingMinutes } from '@/features/attendance/constants'
import type { AttendanceRecord } from '@/features/attendance/types'

const PAGE_SIZE = 31

/** Today's date as a local ISO "yyyy-MM-dd" string (avoids UTC off-by-one near midnight). */
function todayIso(): string {
  const now = new Date()
  const offsetMs = now.getTimezoneOffset() * 60_000
  return new Date(now.getTime() - offsetMs).toISOString().slice(0, 10)
}

/** Columns shared by both tabs (the management tab prepends an Employee column). */
function baseColumns(): DataTableColumn<AttendanceRecord>[] {
  return [
    { id: 'date', header: 'Date', cell: (row) => formatDate(row.attendanceDate) },
    { id: 'type', header: 'Type', cell: (row) => <AttendanceTypeBadge type={row.attendanceType} /> },
    { id: 'in', header: 'Clock in', cell: (row) => formatDateTime(row.clockIn) },
    { id: 'out', header: 'Clock out', cell: (row) => formatDateTime(row.clockOut) },
    { id: 'worked', header: 'Worked', cell: (row) => formatWorkingMinutes(row.workingMinutes) },
    { id: 'status', header: 'Status', cell: (row) => <ApprovalStatusBadge status={row.approvalStatus} /> },
  ]
}

/** The signed-in user's own attendance history. */
function MyAttendanceTab() {
  const [page, setPage] = useState(0)
  const { data, isLoading, isFetching, isError, error, refetch } = useMyAttendance({
    page,
    size: PAGE_SIZE,
    sort: 'attendanceDate,desc',
  })

  // Admin/service accounts aren't linked to an employee profile — the backend 400s
  // rather than returning rows. Show an explanation instead of a scary error panel.
  const notLinked =
    error instanceof ApiError && /not linked to an employee profile/i.test(error.message)

  if (notLinked) {
    return (
      <EmptyState
        icon={UserX}
        title="No personal attendance for this account"
        description="This account isn't linked to an employee profile, so it has no self-service attendance. Use the “All records” tab to view and manage employee attendance."
      />
    )
  }

  return isError ? (
    <ErrorState error={error} onRetry={() => refetch()} />
  ) : (
    <DataTable
      columns={baseColumns()}
      rows={data?.content ?? []}
      getRowId={(row) => row.id}
      isLoading={isLoading || isFetching}
      page={data}
      onPageChange={setPage}
      emptyTitle="No attendance yet"
      emptyDescription="Check in to start recording your attendance."
    />
  )
}

/** Management view: all records, filterable, with approve/reject and corrections. */
function AllAttendanceTab({
  canApprove,
  canEdit,
}: {
  canApprove: boolean
  canEdit: boolean
}) {
  const [page, setPage] = useState(0)
  const [employeeId, setEmployeeId] = useState('')
  // Default to today so the tab loads immediately: the list endpoint requires a
  // date or employeeId filter (else 400).
  const [date, setDate] = useState(todayIso)
  const [toCorrect, setToCorrect] = useState<AttendanceRecord | null>(null)
  const [correctionOpen, setCorrectionOpen] = useState(false)

  const hasFilter = date.trim() !== '' || employeeId.trim() !== ''

  const { data, isLoading, isFetching, isError, error, refetch } = useAttendanceRecords(
    {
      page,
      size: 50,
      sort: 'attendanceDate,desc',
      employeeId: employeeId.trim() ? Number(employeeId) : undefined,
      date: date.trim() || undefined,
    },
    { enabled: hasFilter },
  )
  const { approve, reject } = useAttendanceMutations()

  const decide = (mutation: typeof approve, row: AttendanceRecord, verb: string) => {
    mutation.mutate(row.id, {
      onSuccess: () => toast.success(`${verb} ${row.employeeName}'s attendance`),
      onError: (err) => toast.error(err instanceof ApiError ? err.message : `${verb} failed`),
    })
  }

  const openCorrection = (row: AttendanceRecord | null) => {
    setToCorrect(row)
    setCorrectionOpen(true)
  }

  const columns: DataTableColumn<AttendanceRecord>[] = [
    { id: 'employee', header: 'Employee', cell: (row) => row.employeeName },
    ...baseColumns(),
    {
      id: 'actions',
      header: <span className="sr-only">Actions</span>,
      headerClassName: 'w-12',
      className: 'text-right',
      cell: (row) => {
        const canDecide = canApprove && decidableApprovalStatuses.includes(row.approvalStatus)
        if (!canDecide && !canEdit) return null
        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="size-8"
                aria-label={`Actions for ${row.employeeName}'s attendance`}
              >
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {canDecide ? (
                <>
                  <DropdownMenuItem onClick={() => decide(approve, row, 'Approved')}>
                    <Check className="mr-2 size-4" /> Approve
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    variant="destructive"
                    onClick={() => decide(reject, row, 'Rejected')}
                  >
                    <X className="mr-2 size-4" /> Reject
                  </DropdownMenuItem>
                </>
              ) : null}
              {canDecide && canEdit ? <DropdownMenuSeparator /> : null}
              {canEdit ? (
                <DropdownMenuItem onClick={() => openCorrection(row)}>
                  <Pencil className="mr-2 size-4" /> Correct
                </DropdownMenuItem>
              ) : null}
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
    },
  ]

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div className="flex flex-wrap items-end gap-3">
          <div className="space-y-1">
            <label className="text-xs font-medium text-muted-foreground" htmlFor="filter-employee">
              Employee ID
            </label>
            <Input
              id="filter-employee"
              type="number"
              inputMode="numeric"
              placeholder="All employees"
              className="w-[160px]"
              value={employeeId}
              onChange={(event) => {
                setEmployeeId(event.target.value)
                setPage(0)
              }}
            />
          </div>
          <div className="space-y-1">
            <label className="text-xs font-medium text-muted-foreground" htmlFor="filter-date">
              Date
            </label>
            <Input
              id="filter-date"
              type="date"
              className="w-[170px]"
              value={date}
              onChange={(event) => {
                setDate(event.target.value)
                setPage(0)
              }}
            />
          </div>
        </div>
        <Can anyOf={['ATTENDANCE:EDIT']}>
          <Button variant="outline" onClick={() => openCorrection(null)}>
            <Pencil className="mr-2 size-4" /> Regularize
          </Button>
        </Can>
      </div>

      {!hasFilter ? (
        <EmptyState
          icon={Filter}
          title="Pick a date or employee"
          description="Attendance records are shown for a specific date, or for a single employee over the current month."
        />
      ) : isError ? (
        <ErrorState error={error} onRetry={() => refetch()} />
      ) : (
        <DataTable
          columns={columns}
          rows={data?.content ?? []}
          getRowId={(row) => row.id}
          isLoading={isLoading || isFetching}
          page={data}
          onPageChange={setPage}
          emptyTitle="No attendance records"
          emptyDescription="No records match the current filters."
        />
      )}

      <AttendanceCorrectionDialog
        open={correctionOpen}
        record={toCorrect}
        onOpenChange={(open) => {
          setCorrectionOpen(open)
          if (!open) setToCorrect(null)
        }}
      />
    </div>
  )
}

export function AttendancePage() {
  const { hasAuthority } = useAuth()
  const [checkInOpen, setCheckInOpen] = useState(false)
  const { checkOut } = useAttendanceMutations()

  const canViewAll = hasAuthority('ATTENDANCE:VIEW')

  const handleCheckOut = () => {
    checkOut.mutate(undefined, {
      onSuccess: () => toast.success('Checked out'),
      onError: (err) => toast.error(err instanceof ApiError ? err.message : 'Check-out failed'),
    })
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Attendance"
        description="Check in and out, and review attendance history."
        actions={
          <Can anyOf={['ATTENDANCE:CREATE']}>
            <div className="flex gap-2">
              <Button onClick={() => setCheckInOpen(true)}>
                <LogIn className="mr-2 size-4" /> Check in
              </Button>
              <Button variant="outline" onClick={handleCheckOut} disabled={checkOut.isPending}>
                <LogOut className="mr-2 size-4" /> Check out
              </Button>
            </div>
          </Can>
        }
      />

      <Tabs defaultValue={canViewAll ? 'all' : 'my'}>
        <TabsList>
          <TabsTrigger value="my">My attendance</TabsTrigger>
          {canViewAll ? <TabsTrigger value="all">All records</TabsTrigger> : null}
        </TabsList>
        <TabsContent value="my" className="pt-2">
          <MyAttendanceTab />
        </TabsContent>
        {canViewAll ? (
          <TabsContent value="all" className="pt-2">
            <AllAttendanceTab
              canApprove={hasAuthority('ATTENDANCE:APPROVE')}
              canEdit={hasAuthority('ATTENDANCE:EDIT')}
            />
          </TabsContent>
        ) : null}
      </Tabs>

      <CheckInDialog open={checkInOpen} onOpenChange={setCheckInOpen} />
    </div>
  )
}
