import { apiClient, http } from '@/lib/api/client'
import type { ApiResponse, PageParams, PageResponse } from '@/types/api'
import type { DocumentRecord, DocumentUploadMeta } from './types'

export interface DocumentListParams extends PageParams {
  employeeId?: number
}

/** com.ogm.hrms.controller.DocumentController */
export const documentsApi = {
  list: (params: DocumentListParams) =>
    http.get<PageResponse<DocumentRecord>>('/documents', { params }),

  get: (id: number) => http.get<DocumentRecord>(`/documents/${id}`),

  /** Multipart upload: the binary file part plus metadata form fields. */
  upload: async (file: File, meta: DocumentUploadMeta): Promise<DocumentRecord> => {
    const form = new FormData()
    form.append('file', file)
    form.append('documentType', meta.documentType)
    if (meta.employeeId != null) form.append('employeeId', String(meta.employeeId))
    if (meta.title) form.append('title', meta.title)
    if (meta.folder) form.append('folder', meta.folder)
    if (meta.description) form.append('description', meta.description)
    if (meta.expiryDate) form.append('expiryDate', meta.expiryDate)
    const response = await apiClient.post<ApiResponse<DocumentRecord>>('/documents', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data.data
  },

  delete: (id: number) => http.delete<void>(`/documents/${id}`),

  /** Fetch the raw binary (not wrapped in ApiResponse) for download or inline preview. */
  blob: async (id: number, mode: 'download' | 'preview'): Promise<Blob> => {
    const response = await apiClient.get<Blob>(`/documents/${id}/${mode}`, { responseType: 'blob' })
    return response.data
  },
}
