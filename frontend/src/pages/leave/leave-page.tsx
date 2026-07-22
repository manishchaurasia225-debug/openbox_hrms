import { useState } from 'react'
import { Check, MoreHorizontal, Plus, X } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { PageHeader } from '@/components/common/page-header'
import { ErrorState } from '@/components/common/error-state'
import { ConfirmDialog } from '@/components/common/confirm-dialog'
import { DataTable, type DataTableColumn } from '@/components/common/data-table'
import { Can } from '@/components/auth/require-permission'
import { LeaveStatusBadge } from '@/components/leave/leave-status-badge'
import { ApplyLeaveDialog } from '@/components/leave/apply-leave-dialog'
import {
  LeaveDecisionDialog,
  type LeaveDecision,
} from '@/components/leave/leave-decision-dialog'
import { useAuth } from '@/lib/auth/use-auth'
import { ApiError } from '@/lib/api/client'
import { formatDate, orDash } from '@/lib/format'
import {
  useLeaveMutations,
  useLeaveRequests,
  useMyLeaveRequests,
} from '@/features/leave/hooks'
import {
  cancellableStatuses,
  decidableStatuses,
  leaveStatusFilterOptions,
} from '@/features/leave/constants'
import type { LeaveRequest, LeaveStatus } from '@/features/leave/types'

const PAGE_SIZE = 20

function dateRange(from: string, to: string): string {
  return from === to ? formatDate(from) : `${formatDate(from)} – ${formatDate(to)}`
}

/** The signed-in user's own requests, with the ability to cancel pending ones. */
function MyLeaveTab() {
  const [page, setPage] = useState(0)
  const [toCancel, setToCancel] = useState<LeaveRequest | null>(null)
  const { data, isLoading, isFetching, isError, error, refetch } = useMyLeaveRequests({
    page,
    size: PAGE_SIZE,
    sort: 'fromDate,desc',
  })
  const { cancel } = useLeaveMutations()

  const handleCancel = () => {
    if (!toCancel) return
    cancel.mutate(toCancel.id, {
      onSuccess: () => {
        toast.success('Leave request cancelled')
        setToCancel(null)
      },
      onError: (err) => toast.error(err instanceof ApiError ? err.message : 'Cancel failed'),
    })
  }

  const columns: DataTableColumn<LeaveRequest>[] = [
    { id: 'type', header: 'Type', cell: (row) => row.leaveTypeCode },
    { id: 'dates', header: 'Dates', cell: (row) => dateRange(row.fromDate, row.toDate) },
    { id: 'days', header: 'Days', cell: (row) => (row.halfDay ? `${row.days} (½)` : row.days) },
    { id: 'status', header: 'Status', cell: (row) => <LeaveStatusBadge status={row.status} /> },
    {
      id: 'reason',
      header: 'Reason',
      cell: (row) => <span className="line-clamp-1 text-muted-foreground">{orDash(row.reason)}</span>,
    },
    {
      id: 'actions',
      header: <span className="sr-only">Actions</span>,
      headerClassName: 'w-12',
      className: 'text-right',
      cell: (row) =>
        cancellableStatuses.includes(row.status) ? (
          <Button variant="ghost" size="sm" onClick={() => setToCancel(row)}>
            Cancel
          </Button>
        ) : null,
    },
  ]

  return (
    <>
      {isError ? (
        <ErrorState error={error} onRetry={() => refetch()} />
      ) : (
        <DataTable
          columns={columns}
          rows={data?.content ?? []}
          getRowId={(row) => row.id}
          isLoading={isLoading || isFetching}
          page={data}
          onPageChange={setPage}
          emptyTitle="No leave requests"
          emptyDescription="Apply for leave to see it here."
        />
      )}
      <ConfirmDialog
        open={toCancel !== null}
        onOpenChange={(open) => !open && setToCancel(null)}
        title="Cancel leave request"
        description={
          toCancel ? `Cancel your ${toCancel.leaveTypeCode} request (${dateRange(toCancel.fromDate, toCancel.toDate)})?` : undefined
        }
        confirmLabel="Cancel request"
        cancelLabel="Keep"
        destructive
        loading={cancel.isPending}
        onConfirm={handleCancel}
      />
    </>
  )
}

/** Management view of all requests, with approve/reject for those with LEAVE:APPROVE. */
function AllLeaveTab({ canApprove }: { canApprove: boolean }) {
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [decision, setDecision] = useState<LeaveDecision | null>(null)

  const { data, isLoading, isFetching, isError, error, refetch } = useLeaveRequests({
    page,
    size: PAGE_SIZE,
    sort: 'fromDate,desc',
    status: statusFilter === 'ALL' ? undefined : (statusFilter as LeaveStatus),
  })

  const columns: DataTableColumn<LeaveRequest>[] = [
    { id: 'employee', header: 'Employee', cell: (row) => row.employeeName },
    { id: 'type', header: 'Type', cell: (row) => row.leaveTypeCode },
    { id: 'dates', header: 'Dates', cell: (row) => dateRange(row.fromDate, row.toDate) },
    { id: 'days', header: 'Days', cell: (row) => (row.halfDay ? `${row.days} (½)` : row.days) },
    { id: 'status', header: 'Status', cell: (row) => <LeaveStatusBadge status={row.status} /> },
    {
      id: 'actions',
      header: <span className="sr-only">Actions</span>,
      headerClassName: 'w-12',
      className: 'text-right',
      cell: (row) =>
        canApprove && decidableStatuses.includes(row.status) ? (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label={`Decide on ${row.employeeName}'s request`}>
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => setDecision({ request: row, action: 'approve' })}>
                <Check className="mr-2 size-4" /> Approve
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                variant="destructive"
                onClick={() => setDecision({ request: row, action: 'reject' })}
              >
                <X className="mr-2 size-4" /> Reject
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : null,
    },
  ]

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Select
          value={statusFilter}
          onValueChange={(value) => {
            setStatusFilter(value)
            setPage(0)
          }}
        >
          <SelectTrigger className="w-[200px]">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {leaveStatusFilterOptions.map((option) => (
              <SelectItem key={option.value} value={option.value}>
                {option.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {isError ? (
        <ErrorState error={error} onRetry={() => refetch()} />
      ) : (
        <DataTable
          columns={columns}
          rows={data?.content ?? []}
          getRowId={(row) => row.id}
          isLoading={isLoading || isFetching}
          page={data}
          onPageChange={setPage}
          emptyTitle="No leave requests"
        />
      )}

      <LeaveDecisionDialog decision={decision} onOpenChange={(open) => !open && setDecision(null)} />
    </div>
  )
}

export function LeavePage() {
  const { hasAuthority } = useAuth()
  const [applyOpen, setApplyOpen] = useState(false)
  // Only managers/HR/admin (LEAVE:APPROVE) manage the org-wide queue; employees see just their own.
  const canManage = hasAuthority('LEAVE:APPROVE')

  return (
    <div className="space-y-6">
      <PageHeader
        title="Leave"
        description={canManage ? 'Apply for leave and manage team requests.' : 'Apply for and track your leave.'}
        actions={
          <Can anyOf={['LEAVE:CREATE']}>
            <Button onClick={() => setApplyOpen(true)}>
              <Plus className="mr-2 size-4" /> Apply for leave
            </Button>
          </Can>
        }
      />

      {canManage ? (
        <Tabs defaultValue="my">
          <TabsList>
            <TabsTrigger value="my">My requests</TabsTrigger>
            <TabsTrigger value="all">All requests</TabsTrigger>
          </TabsList>
          <TabsContent value="my" className="pt-2">
            <MyLeaveTab />
          </TabsContent>
          <TabsContent value="all" className="pt-2">
            <AllLeaveTab canApprove />
          </TabsContent>
        </Tabs>
      ) : (
        <MyLeaveTab />
      )}

      <ApplyLeaveDialog open={applyOpen} onOpenChange={setApplyOpen} />
    </div>
  )
}
