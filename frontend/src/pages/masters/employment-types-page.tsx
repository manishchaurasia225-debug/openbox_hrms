import { MasterCrudPage } from '@/components/masters/master-crud-page'
import { employmentTypesConfig } from '@/features/masters/config'

export function EmploymentTypesPage() {
  return <MasterCrudPage config={employmentTypesConfig} />
}
