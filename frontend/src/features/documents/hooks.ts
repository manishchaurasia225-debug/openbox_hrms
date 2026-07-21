import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { documentsApi, type DocumentListParams } from './api'
import type { DocumentUploadMeta } from './types'

export const documentKeys = {
  all: ['documents'] as const,
  list: (params: DocumentListParams) => ['documents', 'list', params] as const,
}

export function useDocuments(params: DocumentListParams) {
  return useQuery({
    queryKey: documentKeys.list(params),
    queryFn: () => documentsApi.list(params),
    placeholderData: keepPreviousData,
  })
}

export function useDocumentMutations() {
  const queryClient = useQueryClient()
  const invalidate = () => queryClient.invalidateQueries({ queryKey: documentKeys.all })

  const upload = useMutation({
    mutationFn: (vars: { file: File; meta: DocumentUploadMeta }) =>
      documentsApi.upload(vars.file, vars.meta),
    onSuccess: invalidate,
  })
  const remove = useMutation({
    mutationFn: (id: number) => documentsApi.delete(id),
    onSuccess: invalidate,
  })

  return { upload, remove }
}

/** Fetch a document's binary and trigger a browser download / open a preview tab. */
export async function openDocument(
  id: number,
  filename: string,
  mode: 'download' | 'preview',
): Promise<void> {
  const blob = await documentsApi.blob(id, mode)
  const url = URL.createObjectURL(blob)
  if (mode === 'preview') {
    window.open(url, '_blank', 'noopener,noreferrer')
  } else {
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    link.remove()
  }
  // Revoke shortly after so the download/preview has time to start.
  setTimeout(() => URL.revokeObjectURL(url), 60_000)
}
