/**
 * Shared API contract types. These mirror the backend's standardized response
 * envelope (com.ogm.hrms.common.ApiResponse) and pagination wrapper
 * (com.ogm.hrms.common.PageResponse) exactly — do not diverge from the backend.
 */

/** A single field-level validation failure (ApiResponse.FieldError). */
export interface FieldError {
  field: string
  message: string
}

/** The single response envelope every HRMS endpoint returns (success and error alike). */
export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  errors?: FieldError[]
  path?: string
  timestamp: string
  requestId?: string
}

/** Transport-friendly pagination envelope carried inside ApiResponse.data on list endpoints. */
export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

/** Query params accepted by paginated/sortable list endpoints (Spring Pageable). */
export interface PageParams {
  page?: number
  size?: number
  /** e.g. "createdAt,desc" — matches Spring's `sort` param. */
  sort?: string
}
