import { Badge } from '@/components/ui/badge'
import { approvalStatusLabels, approvalStatusVariant } from '@/features/attendance/constants'
import type { ApprovalStatus } from '@/features/attendance/types'

export function ApprovalStatusBadge({ status }: { status: ApprovalStatus }) {
  return <Badge variant={approvalStatusVariant[status]}>{approvalStatusLabels[status]}</Badge>
}
