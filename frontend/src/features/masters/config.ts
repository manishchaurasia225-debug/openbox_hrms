/** Per-entity configuration that drives the generic master CRUD screens. */
export interface MasterConfig {
  title: string
  description: string
  /** API base path, e.g. "/departments". */
  basePath: string
  /** React Query cache namespace, e.g. "departments". */
  queryKey: string
  /** Singular noun used in dialogs/toasts, e.g. "department". */
  singular: string
  permissions: {
    view: string
    create: string
    edit: string
    delete: string
  }
}

export const departmentsConfig: MasterConfig = {
  title: 'Departments',
  description: 'Organizational departments used across employee records.',
  basePath: '/departments',
  queryKey: 'departments',
  singular: 'department',
  permissions: {
    view: 'DEPARTMENT:VIEW',
    create: 'DEPARTMENT:CREATE',
    edit: 'DEPARTMENT:EDIT',
    delete: 'DEPARTMENT:DELETE',
  },
}

export const designationsConfig: MasterConfig = {
  title: 'Designations',
  description: 'Job titles / designations assigned to employees.',
  basePath: '/designations',
  queryKey: 'designations',
  singular: 'designation',
  permissions: {
    view: 'DESIGNATION:VIEW',
    create: 'DESIGNATION:CREATE',
    edit: 'DESIGNATION:EDIT',
    delete: 'DESIGNATION:DELETE',
  },
}

export const employmentTypesConfig: MasterConfig = {
  title: 'Employment Types',
  description: 'Employment categories (full-time, contract, intern, …).',
  basePath: '/employment-types',
  queryKey: 'employment-types',
  singular: 'employment type',
  // Employment types are administered under the Settings permission family.
  permissions: {
    view: 'SETTINGS:VIEW',
    create: 'SETTINGS:ADMIN',
    edit: 'SETTINGS:ADMIN',
    delete: 'SETTINGS:ADMIN',
  },
}
