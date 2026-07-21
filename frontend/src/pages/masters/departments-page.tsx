import { MasterCrudPage } from '@/components/masters/master-crud-page'
import { departmentsConfig } from '@/features/masters/config'

export function DepartmentsPage() {
  return <MasterCrudPage config={departmentsConfig} />
}
