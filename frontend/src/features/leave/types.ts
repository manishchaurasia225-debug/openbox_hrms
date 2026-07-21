/** Types mirroring com.ogm.hrms.dto.leave.* — do not diverge from the backend. */
export type LeaveStatus =
  | 'PENDING'
  | 'MANAGER_APPROVED'
  | 'APPROVED'
  | 'REJECTED'
  | 'CANCELLED'

export interface LeaveRequest {
  id: number
  employeeId: number
  employeeName: string
  leaveTypeId: number
  leaveTypeCode: string
  fromDate: string
  toDate: string
  days: number
  halfDay: boolean
  reason?: string
  status: LeaveStatus
  managerApprovedBy?: string
  managerApprovedAt?: string
  hrApprovedBy?: string
  hrApprovedAt?: string
  decisionRemarks?: string
}

export interface ApplyLeaveRequest {
  leaveTypeId: number
  fromDate: string
  toDate: string
  halfDay?: boolean
  reason?: string
}

export interface LeaveDecisionRequest {
  remarks?: string
}

export interface LeaveType {
  id: number
  code: string
  name: string
  description?: string
  defaultAnnualQuota: number
  paid: boolean
  active: boolean
}

export interface LeaveBalance {
  id: number
  employeeId: number
  leaveTypeId: number
  leaveTypeCode: string
  year: number
  allocated: number
  used: number
  remaining: number
}
