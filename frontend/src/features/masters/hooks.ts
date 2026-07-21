import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { masterApi } from './api'
import type { MasterConfig } from './config'
import type { MasterRequest } from './types'
import type { PageParams } from '@/types/api'

export function useMasterList(config: MasterConfig, params: PageParams) {
  return useQuery({
    queryKey: [config.queryKey, 'list', params],
    queryFn: () => masterApi(config.basePath).list(params),
    placeholderData: keepPreviousData,
  })
}

export function useMasterMutations(config: MasterConfig) {
  const queryClient = useQueryClient()
  const api = masterApi(config.basePath)
  const invalidate = () => queryClient.invalidateQueries({ queryKey: [config.queryKey] })

  const create = useMutation({
    mutationFn: (body: MasterRequest) => api.create(body),
    onSuccess: invalidate,
  })
  const update = useMutation({
    mutationFn: (vars: { id: number; body: MasterRequest }) => api.update(vars.id, vars.body),
    onSuccess: invalidate,
  })
  const remove = useMutation({
    mutationFn: (id: number) => api.remove(id),
    onSuccess: invalidate,
  })

  return { create, update, remove }
}
