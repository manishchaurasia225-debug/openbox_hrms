import { useState } from 'react'
import { Download, Eye, MoreHorizontal, Trash2, Upload } from 'lucide-react'
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
import { PageHeader } from '@/components/common/page-header'
import { ErrorState } from '@/components/common/error-state'
import { ConfirmDialog } from '@/components/common/confirm-dialog'
import { DataTable, type DataTableColumn } from '@/components/common/data-table'
import { Can } from '@/components/auth/require-permission'
import { DocumentTypeBadge } from '@/components/documents/document-type-badge'
import { UploadDocumentDialog } from '@/components/documents/upload-document-dialog'
import { ApiError } from '@/lib/api/client'
import { useAuth } from '@/lib/auth/use-auth'
import { formatDate, orDash } from '@/lib/format'
import { openDocument, useDocumentMutations, useDocuments } from '@/features/documents/hooks'
import { formatFileSize } from '@/features/documents/constants'
import type { DocumentRecord } from '@/features/documents/types'

const PAGE_SIZE = 20

const ELEVATED_ROLES = [
  'SUPER_ADMIN',
  'COMPANY_ADMIN',
  'HR_MANAGER',
  'HR_EXECUTIVE',
  'MANAGER',
  'TEAM_LEAD',
  'RECRUITER',
  'FINANCE',
]

export function DocumentsPage() {
  const { user } = useAuth()
  const roles = user?.roles ?? []
  // A standard employee only ever sees their own documents (enforced server-side too), so the
  // employee filter and column are hidden for them.
  const canSeeOthers = roles.some((role) => ELEVATED_ROLES.includes(role))

  const [page, setPage] = useState(0)
  const [employeeId, setEmployeeId] = useState('')
  const [uploadOpen, setUploadOpen] = useState(false)
  const [toDelete, setToDelete] = useState<DocumentRecord | null>(null)

  const { data, isLoading, isFetching, isError, error, refetch } = useDocuments({
    page,
    size: PAGE_SIZE,
    sort: 'createdAt,desc',
    employeeId: employeeId.trim() ? Number(employeeId) : undefined,
  })
  const { remove } = useDocumentMutations()

  const handleOpen = (row: DocumentRecord, mode: 'download' | 'preview') => {
    openDocument(row.id, row.originalFilename, mode).catch((err) =>
      toast.error(err instanceof ApiError ? err.message : `Could not ${mode} the document`),
    )
  }

  const handleDelete = () => {
    if (!toDelete) return
    remove.mutate(toDelete.id, {
      onSuccess: () => {
        toast.success('Document deleted')
        setToDelete(null)
      },
      onError: (err) => toast.error(err instanceof ApiError ? err.message : 'Delete failed'),
    })
  }

  const columns: DataTableColumn<DocumentRecord>[] = [
    {
      id: 'name',
      header: 'Document',
      cell: (row) => (
        <div className="min-w-0">
          <div className="truncate font-medium">{row.title || row.originalFilename}</div>
          <div className="truncate text-xs text-muted-foreground">{row.originalFilename}</div>
        </div>
      ),
    },
    { id: 'type', header: 'Type', cell: (row) => <DocumentTypeBadge type={row.documentType} /> },
    ...(canSeeOthers
      ? [{ id: 'employee', header: 'Employee', cell: (row: DocumentRecord) => orDash(row.employeeId) }]
      : []),
    { id: 'folder', header: 'Folder', cell: (row) => orDash(row.folder) },
    { id: 'size', header: 'Size', cell: (row) => formatFileSize(row.sizeBytes) },
    { id: 'uploaded', header: 'Uploaded', cell: (row) => formatDate(row.createdAt) },
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
              aria-label={`Actions for ${row.originalFilename}`}
            >
              <MoreHorizontal className="size-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => handleOpen(row, 'preview')}>
              <Eye className="mr-2 size-4" /> Preview
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => handleOpen(row, 'download')}>
              <Download className="mr-2 size-4" /> Download
            </DropdownMenuItem>
            <Can anyOf={['DOCUMENT:DELETE']}>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" onClick={() => setToDelete(row)}>
                <Trash2 className="mr-2 size-4" /> Delete
              </DropdownMenuItem>
            </Can>
          </DropdownMenuContent>
        </DropdownMenu>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Documents"
        description="Upload, preview, download, and manage employee and company documents."
        actions={
          <Can anyOf={['DOCUMENT:CREATE']}>
            <Button onClick={() => setUploadOpen(true)}>
              <Upload className="mr-2 size-4" /> Upload
            </Button>
          </Can>
        }
      />

      {canSeeOthers ? (
        <div className="flex items-end gap-3">
          <div className="space-y-1">
            <label className="text-xs font-medium text-muted-foreground" htmlFor="filter-employee">
              Employee ID
            </label>
            <Input
              id="filter-employee"
              type="number"
              inputMode="numeric"
              placeholder="All documents"
              className="w-[180px]"
              value={employeeId}
              onChange={(event) => {
                setEmployeeId(event.target.value)
                setPage(0)
              }}
            />
          </div>
        </div>
      ) : null}

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
          emptyTitle="No documents"
          emptyDescription="Upload a document to see it here."
        />
      )}

      <UploadDocumentDialog open={uploadOpen} onOpenChange={setUploadOpen} />
      <ConfirmDialog
        open={toDelete !== null}
        onOpenChange={(open) => !open && setToDelete(null)}
        title="Delete document"
        description={
          toDelete
            ? `Delete "${toDelete.title || toDelete.originalFilename}"? This cannot be undone.`
            : undefined
        }
        confirmLabel="Delete"
        cancelLabel="Keep"
        destructive
        loading={remove.isPending}
        onConfirm={handleDelete}
      />
    </div>
  )
}
