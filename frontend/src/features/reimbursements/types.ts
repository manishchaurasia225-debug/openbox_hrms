/** Types mirroring com.ogm.hrms.dto.reimbursement.* — do not diverge from the backend. */
export type ReimbursementStatus =
  | 'SUBMITTED'
  | 'MANAGER_APPROVED'
  | 'APPROVED'
  | 'REJECTED'
  | 'PAID'
  | 'CANCELLED'

export type ExpenseCategory = 'TRAVEL' | 'FUEL' | 'FOOD' | 'INTERNET' | 'MEDICAL' | 'OTHER'

export interface Reimbursement {
  id: number
  employeeId: number
  employeeName: string
  category: ExpenseCategory
  amount: number
  expenseDate: string
  description?: string
  billDocumentId?: number
  status: ReimbursementStatus
  managerDecisionBy?: string
  managerDecisionAt?: string
  financeDecisionBy?: string
  financeDecisionAt?: string
  decisionRemarks?: string
  paidAt?: string
}

export interface SubmitReimbursementRequest {
  category: ExpenseCategory
  amount: number
  expenseDate: string
  description?: string
  billDocumentId?: number
}

export interface ReimbursementDecisionRequest {
  remarks?: string
}
