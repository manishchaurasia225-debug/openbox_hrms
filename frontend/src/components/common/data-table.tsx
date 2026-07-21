import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { EmptyState } from '@/components/common/empty-state'
import type { PageResponse } from '@/types/api'

export interface DataTableColumn<T> {
  id: string
  header: ReactNode
  cell: (row: T) => ReactNode
  /** Optional cell/header alignment or width classes. */
  className?: string
  headerClassName?: string
}

interface DataTableProps<T> {
  columns: DataTableColumn<T>[]
  rows: T[]
  getRowId: (row: T) => string | number
  isLoading?: boolean
  /** Pagination metadata + handler; omit for non-paginated tables. */
  page?: Pick<PageResponse<unknown>, 'page' | 'size' | 'totalElements' | 'totalPages' | 'last'>
  onPageChange?: (page: number) => void
  onRowClick?: (row: T) => void
  emptyTitle?: string
  emptyDescription?: string
}

/**
 * Generic, accessible, paginated table driven by the backend's PageResponse.
 * Reused by every list page so pagination and loading/empty behaviour stay
 * consistent across the app.
 */
export function DataTable<T>({
  columns,
  rows,
  getRowId,
  isLoading = false,
  page,
  onPageChange,
  onRowClick,
  emptyTitle = 'No records',
  emptyDescription,
}: DataTableProps<T>) {
  const showSkeleton = isLoading && rows.length === 0
  const showEmpty = !isLoading && rows.length === 0

  return (
    <div className="space-y-3">
      <div className="rounded-lg border">
        <Table>
          <TableHeader>
            <TableRow>
              {columns.map((column) => (
                <TableHead key={column.id} className={column.headerClassName}>
                  {column.header}
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {showSkeleton
              ? Array.from({ length: 8 }).map((_, rowIndex) => (
                  <TableRow key={`skeleton-${rowIndex}`}>
                    {columns.map((column) => (
                      <TableCell key={column.id}>
                        <Skeleton className="h-5 w-full max-w-[160px]" />
                      </TableCell>
                    ))}
                  </TableRow>
                ))
              : rows.map((row) => (
                  <TableRow
                    key={getRowId(row)}
                    onClick={onRowClick ? () => onRowClick(row) : undefined}
                    className={cn(onRowClick && 'cursor-pointer')}
                  >
                    {columns.map((column) => (
                      <TableCell key={column.id} className={column.className}>
                        {column.cell(row)}
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
          </TableBody>
        </Table>

        {showEmpty ? (
          <div className="p-6">
            <EmptyState title={emptyTitle} description={emptyDescription} />
          </div>
        ) : null}
      </div>

      {page && page.totalElements > 0 ? (
        <div className="flex flex-col items-center justify-between gap-3 sm:flex-row">
          <p className="text-sm text-muted-foreground">
            Page {page.page + 1} of {Math.max(page.totalPages, 1)} • {page.totalElements} total
          </p>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page.page <= 0 || isLoading}
              onClick={() => onPageChange?.(page.page - 1)}
            >
              Previous
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={page.last || isLoading}
              onClick={() => onPageChange?.(page.page + 1)}
            >
              Next
            </Button>
          </div>
        </div>
      ) : null}
    </div>
  )
}
