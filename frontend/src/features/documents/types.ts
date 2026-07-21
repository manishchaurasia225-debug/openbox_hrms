/** Types mirroring com.ogm.hrms.dto.document.* — do not diverge from the backend. */

/** com.ogm.hrms.enums.DocumentType */
export type DocumentType =
  | 'RESUME'
  | 'OFFER_LETTER'
  | 'JOINING_LETTER'
  | 'EXPERIENCE_LETTER'
  | 'SALARY_SLIP'
  | 'COMPANY_POLICY'

/** com.ogm.hrms.dto.document.DocumentResponse */
export interface DocumentRecord {
  id: number
  documentType: DocumentType
  employeeId?: number
  title?: string
  originalFilename: string
  contentType?: string
  sizeBytes?: number
  folder?: string
  description?: string
  expiryDate?: string
  createdAt: string
}

/** Metadata sent alongside the multipart file part on upload. */
export interface DocumentUploadMeta {
  documentType: DocumentType
  employeeId?: number
  title?: string
  folder?: string
  description?: string
  expiryDate?: string
}
