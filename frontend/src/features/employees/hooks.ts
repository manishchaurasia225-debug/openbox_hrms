import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { employeesApi } from './api'
import type { EmployeeRequest } from './types'
import type { PageParams } from '@/types/api'

export const employeeKeys = {
  all: ['employees'] as const,
  list: (params: PageParams) => ['employees', 'list', params] as const,
  detail: (id: number) => ['employees', 'detail', id] as const,
}

/** Paginated list; keeps previous page visible while the next loads. */
export function useEmployees(params: PageParams) {
  return useQuery({
    queryKey: employeeKeys.list(params),
    queryFn: () => employeesApi.list(params),
    placeholderData: keepPreviousData,
  })
}

export function useEmployee(id: number | undefined) {
  return useQuery({
    queryKey: employeeKeys.detail(id ?? -1),
    queryFn: () => employeesApi.get(id as number),
    enabled: id != null,
  })
}

export function useCreateEmployee() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: EmployeeRequest) => employeesApi.create(body),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: employeeKeys.all }),
  })
}

export function useUpdateEmployee(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: EmployeeRequest) => employeesApi.update(id, body),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: employeeKeys.all })
      queryClient.setQueryData(employeeKeys.detail(id), updated)
    },
  })
}

export function useDeleteEmployee() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => employeesApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: employeeKeys.all }),
  })
}
