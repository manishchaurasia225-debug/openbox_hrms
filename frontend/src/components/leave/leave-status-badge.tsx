import { Badge } from '@/components/ui/badge'
import { leaveStatusLabels, leaveStatusVariant } from '@/features/leave/constants'
import type { LeaveStatus } from '@/features/leave/types'

export function LeaveStatusBadge({ status }: { status: LeaveStatus }) {
  return <Badge variant={leaveStatusVariant[status]}>{leaveStatusLabels[status]}</Badge>
}
