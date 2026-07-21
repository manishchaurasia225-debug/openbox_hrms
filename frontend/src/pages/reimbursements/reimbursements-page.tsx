import { useState } from 'react'
import { Banknote, Check, MoreHorizontal, Plus, X } from 'lucide-react'
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
import { ReimbursementStatusBadge } from '@/components/reimbursements/reimbursement-status-badge'
import { SubmitReimbursementDialog } from '@/components/reimbursements/submit-reimbursement-dialog'
import {
  ReimbursementDecisionDialog,
  type ReimbursementDecision,
} from '@/components/reimbursements/reimbursement-decision-dialog'
import { useAuth } from '@/lib/auth/use-auth'
import { ApiError } from '@/lib/api/client'
import { formatDate, formatMoney, orDash } from '@/lib/format'
import {
  useMyReimbursements,
  useReimbursementMutations,
  useReimbursements,
} from '@/features/reimbursements/hooks'
import {
  cancellableStatuses,
  decidableStatuses,
  expenseCategoryLabels,
  payableStatuses,
  reimbursementStatusFilterOptions,
} from '@/features/reimbursements/constants'
import type { Reimbursement, ReimbursementStatus } from '@/features/reimbursements/types'

const PAGE_SIZE = 20

/** The signed-in user's own claims, with the ability to cancel pending ones. */
function MyClaimsTab() {
  const [page, setPage] = useState(0)
  const [toCancel, setToCancel] = useState<Reimbursement | null>(null)
  const { data, isLoading, isFetching, isError, error, refetch } = useMyReimbursements({
    page,
    size: PAGE_SIZE,
    sort: 'expenseDate,desc',
  })
  const { cancel } = useReimbursementMutations()

  const handleCancel = () => {
    if (!toCancel) return
    cancel.mutate(toCancel.id, {
      onSuccess: () => {
        toast.success('Claim cancelled')
        setToCancel(null)
      },
      onError: (err) => toast.error(err instanceof ApiError ? err.message : 'Cancel failed'),
    })
  }

  const columns: DataTableColumn<Reimbursement>[] = [
    { id: 'category', header: 'Category', cell: (row) => expenseCategoryLabels[row.category] },
    { id: 'amount', header: 'Amount', cell: (row) => formatMoney(row.amount) },
    { id: 'date', header: 'Expense date', cell: (row) => formatDate(row.expenseDate) },
    { id: 'status', header: 'Status', cell: (row) => <ReimbursementStatusBadge status={row.status} /> },
    {
      id: 'description',
      header: 'Description',
      cell: (row) => <span className="line-clamp-1 text-muted-foreground">{orDash(row.description)}</span>,
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
          emptyTitle="No claims"
          emptyDescription="Submit a reimbursement claim to see it here."
        />
      )}
      <ConfirmDialog
        open={toCancel !== null}
        onOpenChange={(open) => !open && setToCancel(null)}
        title="Cancel claim"
        description={
          toCancel
            ? `Cancel your ${expenseCategoryLabels[toCancel.category]} claim of ${formatMoney(toCancel.amount)}?`
            : undefined
        }
        confirmLabel="Cancel claim"
        cancelLabel="Keep"
        destructive
        loading={cancel.isPending}
        onConfirm={handleCancel}
      />
    </>
  )
}

/** Management view: approve/reject/pay across all claims. */
function AllClaimsTab({ canApprove }: { canApprove: boolean }) {
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [decision, setDecision] = useState<ReimbursementDecision | null>(null)
  const [toPay, setToPay] = useState<Reimbursement | null>(null)

  const { data, isLoading, isFetching, isError, error, refetch } = useReimbursements({
    page,
    size: PAGE_SIZE,
    sort: 'expenseDate,desc',
    status: statusFilter === 'ALL' ? undefined : (statusFilter as ReimbursementStatus),
  })
  const { pay } = useReimbursementMutations()

  const handlePay = () => {
    if (!toPay) return
    pay.mutate(toPay.id, {
      onSuccess: () => {
        toast.success('Claim marked as paid')
        setToPay(null)
      },
      onError: (err) => toast.error(err instanceof ApiError ? err.message : 'Payment failed'),
    })
  }

  const columns: DataTableColumn<Reimbursement>[] = [
    { id: 'employee', header: 'Employee', cell: (row) => row.employeeName },
    { id: 'category', header: 'Category', cell: (row) => expenseCategoryLabels[row.category] },
    { id: 'amount', header: 'Amount', cell: (row) => formatMoney(row.amount) },
    { id: 'date', header: 'Expense date', cell: (row) => formatDate(row.expenseDate) },
    { id: 'status', header: 'Status', cell: (row) => <ReimbursementStatusBadge status={row.status} /> },
    {
      id: 'actions',
      header: <span className="sr-only">Actions</span>,
      headerClassName: 'w-12',
      className: 'text-right',
      cell: (row) => {
        if (!canApprove) return null
        const canDecide = decidableStatuses.includes(row.status)
        const canPay = payableStatuses.includes(row.status)
        if (!canDecide && !canPay) return null
        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8" aria-label={`Actions for ${row.employeeName}'s claim`}>
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {canDecide ? (
                <>
                  <DropdownMenuItem onClick={() => setDecision({ claim: row, action: 'approve' })}>
                    <Check className="mr-2 size-4" /> Approve
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    variant="destructive"
                    onClick={() => setDecision({ claim: row, action: 'reject' })}
                  >
                    <X className="mr-2 size-4" /> Reject
                  </DropdownMenuItem>
                </>
              ) : null}
              {canDecide && canPay ? <DropdownMenuSeparator /> : null}
              {canPay ? (
                <DropdownMenuItem onClick={() => setToPay(row)}>
                  <Banknote className="mr-2 size-4" /> Mark as paid
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
            {reimbursementStatusFilterOptions.map((option) => (
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
          emptyTitle="No claims"
        />
      )}

      <ReimbursementDecisionDialog
        decision={decision}
        onOpenChange={(open) => !open && setDecision(null)}
      />
      <ConfirmDialog
        open={toPay !== null}
        onOpenChange={(open) => !open && setToPay(null)}
        title="Mark claim as paid"
        description={
          toPay
            ? `Mark ${toPay.employeeName}'s ${formatMoney(toPay.amount)} claim as paid?`
            : undefined
        }
        confirmLabel="Mark paid"
        loading={pay.isPending}
        onConfirm={handlePay}
      />
    </div>
  )
}

export function ReimbursementsPage() {
  const { hasAuthority } = useAuth()
  const [submitOpen, setSubmitOpen] = useState(false)

  return (
    <div className="space-y-6">
      <PageHeader
        title="Reimbursements"
        description="Submit expense claims and manage approvals."
        actions={
          <Can anyOf={['EXPENSE:CREATE']}>
            <Button onClick={() => setSubmitOpen(true)}>
              <Plus className="mr-2 size-4" /> New claim
            </Button>
          </Can>
        }
      />

      <Tabs defaultValue="my">
        <TabsList>
          <TabsTrigger value="my">My claims</TabsTrigger>
          <TabsTrigger value="all">All claims</TabsTrigger>
        </TabsList>
        <TabsContent value="my" className="pt-2">
          <MyClaimsTab />
        </TabsContent>
        <TabsContent value="all" className="pt-2">
          <AllClaimsTab canApprove={hasAuthority('EXPENSE:APPROVE')} />
        </TabsContent>
      </Tabs>

      <SubmitReimbursementDialog open={submitOpen} onOpenChange={setSubmitOpen} />
    </div>
  )
}
