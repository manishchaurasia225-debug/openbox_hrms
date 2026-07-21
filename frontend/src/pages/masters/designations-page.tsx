import { MasterCrudPage } from '@/components/masters/master-crud-page'
import { designationsConfig } from '@/features/masters/config'

export function DesignationsPage() {
  return <MasterCrudPage config={designationsConfig} />
}
