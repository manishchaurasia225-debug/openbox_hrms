import { Badge } from '@/components/ui/badge'
import { documentTypeLabels, documentTypeVariant } from '@/features/documents/constants'
import type { DocumentType } from '@/features/documents/types'

export function DocumentTypeBadge({ type }: { type: DocumentType }) {
  return <Badge variant={documentTypeVariant[type]}>{documentTypeLabels[type]}</Badge>
}
