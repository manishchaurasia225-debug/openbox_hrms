/**
 * Shared shape for simple org master records (Departments, Designations,
 * Employment Types, …). All expose the same {code, name, description, active}
 * contract on the backend, so one feature module serves them all.
 */
export interface MasterRecord {
  id: number
  code: string
  name: string
  description?: string
  active: boolean
  createdAt?: string
  updatedAt?: string
}

export interface MasterRequest {
  code: string
  name: string
  description?: string
  active?: boolean
}
