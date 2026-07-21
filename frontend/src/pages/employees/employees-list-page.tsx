import { useState } from 'react'
import { MoreHorizontal, Plus } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Badge } from '@/components/ui/badge'
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
import { PageHeader } from '@/components/common/page-header'
import { ErrorState } from '@/components/common/error-state'
import { ConfirmDialog } from '@/components/common/confirm-dialog'
import { DataTable, type DataTableColumn } from '@/components/common/data-table'
import { Can } from '@/components/auth/require-permission'
import { useAuth } from '@/lib/auth/use-auth'
import { formatDate } from '@/lib/format'
import { ApiError } from '@/lib/api/client'
import { useDeleteEmployee, useEmployees } from '@/features/employees/hooks'
import { employmentStatusLabels, employmentStatusVariant } from '@/features/employees/constants'
import type { Employee } from '@/features/employees/types'

const PAGE_SIZE = 20

const sortOptions = [
  { value: 'createdAt,desc', label: 'Newest first' },
  { value: 'createdAt,asc', label: 'Oldest first' },
  { value: 'fullName,asc', label: 'Name (A–Z)' },
  { value: 'fullName,desc', label: 'Name (Z–A)' },
  { value: 'employeeCode,asc', label: 'Code (ascending)' },
]

export function EmployeesListPage() {
  const navigate = useNavigate()
  const { hasAuthority } = useAuth()
  const [page, setPage] = useState(0)
  const [sort, setSort] = useState('createdAt,desc')
  const [toDelete, setToDelete] = useState<Employee | null>(null)

  const { data, isLoading, isError, error, refetch, isFetching } = useEmployees({
    page,
    size: PAGE_SIZE,
    sort,
  })
  const deleteEmployee = useDeleteEmployee()

  const canEdit = hasAuthority('EMPLOYEE:EDIT')
  const canDelete = hasAuthority('EMPLOYEE:DELETE')

  const handleDelete = () => {
    if (!toDelete) return
    deleteEmployee.mutate(toDelete.id, {
      onSuccess: () => {
        toast.success(`${toDelete.fullName} deleted`)
        setToDelete(null)
      },
      onError: (err) => {
        toast.error(err instanceof ApiError ? err.message : 'Failed to delete employee')
      },
    })
  }

  const columns: DataTableColumn<Employee>[] = [
    {
      id: 'employeeCode',
      header: 'Code',
      cell: (row) => <span className="font-medium">{row.employeeCode}</span>,
      className: 'font-medium',
    },
    { id: 'fullName', header: 'Name', cell: (row) => row.fullName },
    {
      id: 'department',
      header: 'Department',
      cell: (row) => row.employment?.departmentName ?? '—',
    },
    {
      id: 'designation',
      header: 'Designation',
      cell: (row) => row.employment?.designationName ?? '—',
    },
    {
      id: 'status',
      header: 'Status',
      cell: (row) =>
        row.employment?.employmentStatus ? (
          <Badge variant={employmentStatusVariant[row.employment.employmentStatus]}>
            {employmentStatusLabels[row.employment.employmentStatus]}
          </Badge>
        ) : (
          '—'
        ),
    },
    {
      id: 'joiningDate',
      header: 'Joined',
      cell: (row) => formatDate(row.employment?.joiningDate),
    },
    {
      id: 'actions',
      header: <span className="sr-only">Actions</span>,
      headerClassName: 'w-12',
      className: 'text-right',
      cell: (row) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              size="icon"
              className="size-8"
              aria-label={`Actions for ${row.fullName}`}
              onClick={(event) => event.stopPropagation()}
            >
              <MoreHorizontal className="size-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" onClick={(event) => event.stopPropagation()}>
            <DropdownMenuItem onClick={() => navigate(`/employees/${row.id}`)}>
              View
            </DropdownMenuItem>
            {canEdit ? (
              <DropdownMenuItem onClick={() => navigate(`/employees/${row.id}/edit`)}>
                Edit
              </DropdownMenuItem>
            ) : null}
            {canDelete ? (
              <>
                <DropdownMenuSeparator />
                <DropdownMenuItem variant="destructive" onClick={() => setToDelete(row)}>
                  Delete
                </DropdownMenuItem>
              </>
            ) : null}
          </DropdownMenuContent>
        </DropdownMenu>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Employees"
        description="Manage the employee master directory."
        actions={
          <Can anyOf={['EMPLOYEE:CREATE']}>
            <Button onClick={() => navigate('/employees/new')}>
              <Plus className="mr-2 size-4" /> Add employee
            </Button>
          </Can>
        }
      />

      <div className="flex justify-end">
        <Select
          value={sort}
          onValueChange={(value) => {
            setSort(value)
            setPage(0)
          }}
        >
          <SelectTrigger className="w-[200px]">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {sortOptions.map((option) => (
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
          onRowClick={(row) => navigate(`/employees/${row.id}`)}
          emptyTitle="No employees yet"
          emptyDescription="Add your first employee to get started."
        />
      )}

      <ConfirmDialog
        open={toDelete !== null}
        onOpenChange={(open) => !open && setToDelete(null)}
        title="Delete employee"
        description={
          toDelete
            ? `Delete ${toDelete.fullName} (${toDelete.employeeCode})? This action cannot be undone.`
            : undefined
        }
        confirmLabel="Delete"
        destructive
        loading={deleteEmployee.isPending}
        onConfirm={handleDelete}
      />
    </div>
  )
}
