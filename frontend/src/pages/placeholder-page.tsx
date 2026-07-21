import { Hammer } from 'lucide-react'
import { PageHeader } from '@/components/common/page-header'
import { EmptyState } from '@/components/common/empty-state'

/**
 * Temporary page for routes whose feature UI is not built yet. The route,
 * layout, and RBAC gating are already in place — only the page body is pending.
 */
export function PlaceholderPage({ title }: { title: string }) {
  return (
    <div className="space-y-6">
      <PageHeader title={title} />
      <EmptyState
        icon={Hammer}
        title="Coming soon"
        description={`The ${title} module is scaffolded and will be implemented next.`}
      />
    </div>
  )
}
