import { useQuery } from '@tanstack/react-query'
import { http } from '@/lib/api/client'
import type { PageResponse } from '@/types/api'

/** A selectable org-master record (department, designation, employment type). */
export interface OrgOption {
  id: number
  code: string
  name: string
  active: boolean
}

/**
 * Master lists are small, so fetch a single large page and sort by name client-side
 * for use in form dropdowns. Only active records are offered.
 */
async function fetchOptions(path: string): Promise<OrgOption[]> {
  const page = await http.get<PageResponse<OrgOption>>(path, { params: { page: 0, size: 200 } })
  return page.content
    .filter((item) => item.active)
    .sort((a, b) => a.name.localeCompare(b.name))
}

function useOrgOptions(key: string, path: string) {
  return useQuery({
    queryKey: ['org-options', key],
    queryFn: () => fetchOptions(path),
    staleTime: 5 * 60_000,
  })
}

export const useDepartmentOptions = () => useOrgOptions('departments', '/departments')
export const useDesignationOptions = () => useOrgOptions('designations', '/designations')
export const useEmploymentTypeOptions = () => useOrgOptions('employment-types', '/employment-types')
