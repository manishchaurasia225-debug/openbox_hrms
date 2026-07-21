import { http } from '@/lib/api/client'
import type { PageParams, PageResponse } from '@/types/api'
import type { Employee, EmployeeRequest } from './types'

/** com.ogm.hrms.controller.EmployeeController — /api/v1/employees */
export const employeesApi = {
  list: (params: PageParams) =>
    http.get<PageResponse<Employee>>('/employees', { params }),
  get: (id: number) => http.get<Employee>(`/employees/${id}`),
  create: (body: EmployeeRequest) => http.post<Employee>('/employees', body),
  update: (id: number, body: EmployeeRequest) => http.put<Employee>(`/employees/${id}`, body),
  remove: (id: number) => http.delete<void>(`/employees/${id}`),
}
