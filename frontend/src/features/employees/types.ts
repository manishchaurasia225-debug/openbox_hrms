/**
 * Types mirroring com.ogm.hrms.dto.employee.EmployeeRequest / EmployeeResponse.
 * The response Employment carries resolved master names; the request uses ids only.
 */
export type Gender = 'MALE' | 'FEMALE' | 'OTHER' | 'UNDISCLOSED'
export type MaritalStatus = 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED' | 'OTHER'
export type EmploymentStatus = 'ACTIVE' | 'ON_NOTICE' | 'RESIGNED' | 'TERMINATED' | 'RETIRED'

export interface EmployeeContact {
  mobile?: string
  personalEmail?: string
  officialEmail?: string
  currentAddress?: string
  permanentAddress?: string
}

export interface EmployeeSalary {
  basicSalary?: number
  hra?: number
  specialAllowance?: number
  bonus?: number
  incentives?: number
  otherAllowances?: number
}

export interface EmployeeBank {
  bankName?: string
  accountNumber?: string
  ifscCode?: string
  accountHolderName?: string
}

export interface EmployeeGovernmentIds {
  pan?: string
  aadhaar?: string
  passport?: string
  drivingLicense?: string
}

export interface EmployeeSocial {
  linkedinUrl?: string
  instagramUrl?: string
  facebookUrl?: string
  xUrl?: string
}

/** Employment as returned (with resolved names). */
export interface EmployeeEmployment {
  departmentId?: number
  departmentName?: string
  designationId?: number
  designationName?: string
  employmentTypeId?: number
  employmentTypeName?: string
  joiningDate?: string
  endDate?: string
  noticePeriodDays?: number
  employmentStatus?: EmploymentStatus
}

export interface Employee {
  id: number
  employeeCode: string
  fullName: string
  gender?: Gender
  dateOfBirth?: string
  bloodGroup?: string
  nationality?: string
  maritalStatus?: MaritalStatus
  photoUrl?: string
  contact?: EmployeeContact
  employment?: EmployeeEmployment
  salary?: EmployeeSalary
  bank?: EmployeeBank
  governmentIds?: EmployeeGovernmentIds
  social?: EmployeeSocial
  userId?: number
  createdAt?: string
  updatedAt?: string
}

/** Employment as sent on create/update (ids only). */
export interface EmployeeEmploymentRequest {
  departmentId?: number
  designationId?: number
  employmentTypeId?: number
  joiningDate?: string
  endDate?: string
  noticePeriodDays?: number
  employmentStatus?: EmploymentStatus
}

export interface EmployeeRequest {
  employeeCode: string
  fullName: string
  gender?: Gender
  dateOfBirth?: string
  bloodGroup?: string
  nationality?: string
  maritalStatus?: MaritalStatus
  photoUrl?: string
  contact?: EmployeeContact
  employment?: EmployeeEmploymentRequest
  salary?: EmployeeSalary
  bank?: EmployeeBank
  governmentIds?: EmployeeGovernmentIds
  social?: EmployeeSocial
  userId?: number
}
