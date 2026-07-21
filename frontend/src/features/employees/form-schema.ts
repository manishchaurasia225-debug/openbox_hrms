import { z } from 'zod'
import type {
  Employee,
  EmployeeRequest,
  EmploymentStatus,
  Gender,
  MaritalStatus,
} from './types'

/**
 * Form schema. All inputs are strings (text/number/date/select), so validation
 * mirrors the backend's @Size/@NotBlank rules; type coercion to the request DTO
 * happens in toRequest(). Only employeeCode and fullName are required.
 */
export const employeeFormSchema = z.object({
  employeeCode: z.string().trim().min(1, 'Employee code is required').max(40),
  fullName: z.string().trim().min(1, 'Full name is required').max(150),
  gender: z.string(),
  dateOfBirth: z.string(),
  bloodGroup: z.string().max(10),
  nationality: z.string().max(60),
  maritalStatus: z.string(),
  photoUrl: z.string().max(500),
  contact: z.object({
    mobile: z.string().max(30),
    personalEmail: z.string().max(190),
    officialEmail: z.string().max(190),
    currentAddress: z.string().max(300),
    permanentAddress: z.string().max(300),
  }),
  employment: z.object({
    departmentId: z.string(),
    designationId: z.string(),
    employmentTypeId: z.string(),
    joiningDate: z.string(),
    endDate: z.string(),
    noticePeriodDays: z.string(),
    employmentStatus: z.string(),
  }),
  salary: z.object({
    basicSalary: z.string(),
    hra: z.string(),
    specialAllowance: z.string(),
    bonus: z.string(),
    incentives: z.string(),
    otherAllowances: z.string(),
  }),
  bank: z.object({
    bankName: z.string().max(120),
    accountNumber: z.string().max(40),
    ifscCode: z.string().max(20),
    accountHolderName: z.string().max(120),
  }),
  governmentIds: z.object({
    pan: z.string().max(20),
    aadhaar: z.string().max(20),
    passport: z.string().max(20),
    drivingLicense: z.string().max(30),
  }),
  social: z.object({
    linkedinUrl: z.string().max(200),
    instagramUrl: z.string().max(200),
    facebookUrl: z.string().max(200),
    xUrl: z.string().max(200),
  }),
})

export type EmployeeFormValues = z.infer<typeof employeeFormSchema>

export const emptyEmployeeForm: EmployeeFormValues = {
  employeeCode: '',
  fullName: '',
  gender: '',
  dateOfBirth: '',
  bloodGroup: '',
  nationality: '',
  maritalStatus: '',
  photoUrl: '',
  contact: { mobile: '', personalEmail: '', officialEmail: '', currentAddress: '', permanentAddress: '' },
  employment: {
    departmentId: '',
    designationId: '',
    employmentTypeId: '',
    joiningDate: '',
    endDate: '',
    noticePeriodDays: '',
    employmentStatus: '',
  },
  salary: {
    basicSalary: '',
    hra: '',
    specialAllowance: '',
    bonus: '',
    incentives: '',
    otherAllowances: '',
  },
  bank: { bankName: '', accountNumber: '', ifscCode: '', accountHolderName: '' },
  governmentIds: { pan: '', aadhaar: '', passport: '', drivingLicense: '' },
  social: { linkedinUrl: '', instagramUrl: '', facebookUrl: '', xUrl: '' },
}

const s = (value: string | number | null | undefined): string => (value == null ? '' : String(value))

/** Map an existing employee into editable form values. */
export function toFormValues(employee: Employee): EmployeeFormValues {
  return {
    employeeCode: s(employee.employeeCode),
    fullName: s(employee.fullName),
    gender: s(employee.gender),
    dateOfBirth: s(employee.dateOfBirth),
    bloodGroup: s(employee.bloodGroup),
    nationality: s(employee.nationality),
    maritalStatus: s(employee.maritalStatus),
    photoUrl: s(employee.photoUrl),
    contact: {
      mobile: s(employee.contact?.mobile),
      personalEmail: s(employee.contact?.personalEmail),
      officialEmail: s(employee.contact?.officialEmail),
      currentAddress: s(employee.contact?.currentAddress),
      permanentAddress: s(employee.contact?.permanentAddress),
    },
    employment: {
      departmentId: s(employee.employment?.departmentId),
      designationId: s(employee.employment?.designationId),
      employmentTypeId: s(employee.employment?.employmentTypeId),
      joiningDate: s(employee.employment?.joiningDate),
      endDate: s(employee.employment?.endDate),
      noticePeriodDays: s(employee.employment?.noticePeriodDays),
      employmentStatus: s(employee.employment?.employmentStatus),
    },
    salary: {
      basicSalary: s(employee.salary?.basicSalary),
      hra: s(employee.salary?.hra),
      specialAllowance: s(employee.salary?.specialAllowance),
      bonus: s(employee.salary?.bonus),
      incentives: s(employee.salary?.incentives),
      otherAllowances: s(employee.salary?.otherAllowances),
    },
    bank: {
      bankName: s(employee.bank?.bankName),
      accountNumber: s(employee.bank?.accountNumber),
      ifscCode: s(employee.bank?.ifscCode),
      accountHolderName: s(employee.bank?.accountHolderName),
    },
    governmentIds: {
      pan: s(employee.governmentIds?.pan),
      aadhaar: s(employee.governmentIds?.aadhaar),
      passport: s(employee.governmentIds?.passport),
      drivingLicense: s(employee.governmentIds?.drivingLicense),
    },
    social: {
      linkedinUrl: s(employee.social?.linkedinUrl),
      instagramUrl: s(employee.social?.instagramUrl),
      facebookUrl: s(employee.social?.facebookUrl),
      xUrl: s(employee.social?.xUrl),
    },
  }
}

const str = (value: string): string | undefined => {
  const trimmed = value.trim()
  return trimmed === '' ? undefined : trimmed
}

const num = (value: string): number | undefined => {
  const trimmed = value.trim()
  if (trimmed === '') return undefined
  const parsed = Number(trimmed)
  return Number.isFinite(parsed) ? parsed : undefined
}

/** Convert form values into the backend request payload (empty -> omitted). */
export function toRequest(values: EmployeeFormValues): EmployeeRequest {
  return {
    employeeCode: values.employeeCode.trim(),
    fullName: values.fullName.trim(),
    gender: str(values.gender) as Gender | undefined,
    dateOfBirth: str(values.dateOfBirth),
    bloodGroup: str(values.bloodGroup),
    nationality: str(values.nationality),
    maritalStatus: str(values.maritalStatus) as MaritalStatus | undefined,
    photoUrl: str(values.photoUrl),
    contact: {
      mobile: str(values.contact.mobile),
      personalEmail: str(values.contact.personalEmail),
      officialEmail: str(values.contact.officialEmail),
      currentAddress: str(values.contact.currentAddress),
      permanentAddress: str(values.contact.permanentAddress),
    },
    employment: {
      departmentId: num(values.employment.departmentId),
      designationId: num(values.employment.designationId),
      employmentTypeId: num(values.employment.employmentTypeId),
      joiningDate: str(values.employment.joiningDate),
      endDate: str(values.employment.endDate),
      noticePeriodDays: num(values.employment.noticePeriodDays),
      employmentStatus: str(values.employment.employmentStatus) as EmploymentStatus | undefined,
    },
    salary: {
      basicSalary: num(values.salary.basicSalary),
      hra: num(values.salary.hra),
      specialAllowance: num(values.salary.specialAllowance),
      bonus: num(values.salary.bonus),
      incentives: num(values.salary.incentives),
      otherAllowances: num(values.salary.otherAllowances),
    },
    bank: {
      bankName: str(values.bank.bankName),
      accountNumber: str(values.bank.accountNumber),
      ifscCode: str(values.bank.ifscCode),
      accountHolderName: str(values.bank.accountHolderName),
    },
    governmentIds: {
      pan: str(values.governmentIds.pan),
      aadhaar: str(values.governmentIds.aadhaar),
      passport: str(values.governmentIds.passport),
      drivingLicense: str(values.governmentIds.drivingLicense),
    },
    social: {
      linkedinUrl: str(values.social.linkedinUrl),
      instagramUrl: str(values.social.instagramUrl),
      facebookUrl: str(values.social.facebookUrl),
      xUrl: str(values.social.xUrl),
    },
  }
}
