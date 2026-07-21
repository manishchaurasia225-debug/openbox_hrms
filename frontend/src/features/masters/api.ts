import { http } from '@/lib/api/client'
import type { PageParams, PageResponse } from '@/types/api'
import type { MasterRecord, MasterRequest } from './types'

/** CRUD client bound to a master entity's base path (e.g. "/departments"). */
export function masterApi(basePath: string) {
  return {
    list: (params: PageParams) =>
      http.get<PageResponse<MasterRecord>>(basePath, { params }),
    get: (id: number) => http.get<MasterRecord>(`${basePath}/${id}`),
    create: (body: MasterRequest) => http.post<MasterRecord>(basePath, body),
    update: (id: number, body: MasterRequest) =>
      http.put<MasterRecord>(`${basePath}/${id}`, body),
    remove: (id: number) => http.delete<void>(`${basePath}/${id}`),
  }
}
