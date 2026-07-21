import { Badge } from '@/components/ui/badge'
import {
  reimbursementStatusLabels,
  reimbursementStatusVariant,
} from '@/features/reimbursements/constants'
import type { ReimbursementStatus } from '@/features/reimbursements/types'

export function ReimbursementStatusBadge({ status }: { status: ReimbursementStatus }) {
  return (
    <Badge variant={reimbursementStatusVariant[status]}>
      {reimbursementStatusLabels[status]}
    </Badge>
  )
}
