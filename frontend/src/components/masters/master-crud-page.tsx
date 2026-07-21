import { useState } from 'react'
import { MoreHorizontal, Plus } from 'lucide-react'
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
import { PageHeader } from '@/components/common/page-header'
import { ErrorState } from '@/components/common/error-state'
import { ConfirmDialog } from '@/components/common/confirm-dialog'
import { DataTable, type DataTableColumn } from '@/components/common/data-table'
import { Can } from '@/components/auth/require-permission'
import { MasterFormDialog } from '@/components/masters/master-form-dialog'
import { useAuth } from '@/lib/auth/use-auth'
import { ApiError } from '@/lib/api/client'
import { orDash } from '@/lib/format'
import { useMasterList, useMasterMutations } from '@/features/masters/hooks'
import type { MasterConfig } from '@/features/masters/config'
import type { MasterRecord } from '@/features/masters/types'

const PAGE_SIZE = 20

/** Generic list + create/edit/delete screen for a simple {code,name,description,active} master. */
export function MasterCrudPage({ config }: { config: MasterConfig }) {
  const { hasAuthority } = useAuth()
  const [page, setPage] = useState(0)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<MasterRecord | null>(null)
  const [toDelete, setToDelete] = useState<MasterRecord | null>(null)

  const { data, isLoading, isFetching, isError, error, refetch } = useMasterList(config, {
    page,
    size: PAGE_SIZE,
    sort: 'name,asc',
  })
  const { remove } = useMasterMutations(config)

  const canEdit = hasAuthority(config.permissions.edit)
  const canDelete = hasAuthority(config.permissions.delete)

  const openCreate = () => {
    setEditing(null)
    setFormOpen(true)
  }
  const openEdit = (record: MasterRecord) => {
    setEditing(record)
    setFormOpen(true)
  }

  const handleDelete = () => {
    if (!toDelete) return
    remove.mutate(toDelete.id, {
      onSuccess: () => {
        toast.success(`${toDelete.name} deleted`)
        setToDelete(null)
      },
      onError: (err) => toast.error(err instanceof ApiError ? err.message : 'Delete failed'),
    })
  }

  const columns: DataTableColumn<MasterRecord>[] = [
    {
      id: 'code',
      header: 'Code',
      cell: (row) => <span className="font-medium">{row.code}</span>,
    },
    { id: 'name', header: 'Name', cell: (row) => row.name },
    {
      id: 'description',
      header: 'Description',
      cell: (row) => <span className="line-clamp-1 text-muted-foreground">{orDash(row.description)}</span>,
    },
    {
      id: 'active',
      header: 'Status',
      cell: (row) => (
        <Badge variant={row.active ? 'default' : 'outline'}>
          {row.active ? 'Active' : 'Inactive'}
        </Badge>
      ),
    },
    {
      id: 'actions',
      header: <span className="sr-only">Actions</span>,
      headerClassName: 'w-12',
      className: 'text-right',
      cell: (row) =>
        canEdit || canDelete ? (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="size-8"
                aria-label={`Actions for ${row.name}`}
              >
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {canEdit ? (
                <DropdownMenuItem onClick={() => openEdit(row)}>Edit</DropdownMenuItem>
              ) : null}
              {canDelete ? (
                <>
                  {canEdit ? <DropdownMenuSeparator /> : null}
                  <DropdownMenuItem variant="destructive" onClick={() => setToDelete(row)}>
                    Delete
                  </DropdownMenuItem>
                </>
              ) : null}
            </DropdownMenuContent>
          </DropdownMenu>
        ) : null,
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title={config.title}
        description={config.description}
        actions={
          <Can anyOf={[config.permissions.create]}>
            <Button onClick={openCreate}>
              <Plus className="mr-2 size-4" /> Add {config.singular}
            </Button>
          </Can>
        }
      />

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
          emptyTitle={`No ${config.title.toLowerCase()} yet`}
          emptyDescription={
            canEdit || hasAuthority(config.permissions.create)
              ? `Add your first ${config.singular} to get started.`
              : undefined
          }
        />
      )}

      <MasterFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        config={config}
        record={editing}
      />

      <ConfirmDialog
        open={toDelete !== null}
        onOpenChange={(open) => !open && setToDelete(null)}
        title={`Delete ${config.singular}`}
        description={
          toDelete ? `Delete ${toDelete.name} (${toDelete.code})? This cannot be undone.` : undefined
        }
        confirmLabel="Delete"
        destructive
        loading={remove.isPending}
        onConfirm={handleDelete}
      />
    </div>
  )
}
