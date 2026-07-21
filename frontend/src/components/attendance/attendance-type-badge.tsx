import { Badge } from '@/components/ui/badge'
import { attendanceTypeLabels, attendanceTypeVariant } from '@/features/attendance/constants'
import type { AttendanceType } from '@/features/attendance/types'

export function AttendanceTypeBadge({ type }: { type: AttendanceType }) {
  return <Badge variant={attendanceTypeVariant[type]}>{attendanceTypeLabels[type]}</Badge>
}
