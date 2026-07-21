/** Types mirroring com.ogm.hrms.dto.dashboard.* — do not diverge from the backend. */

/** com.ogm.hrms.dto.dashboard.CountEntry */
export interface CountEntry {
  label: string
  count: number
}

/** com.ogm.hrms.dto.dashboard.PersonDate */
export interface PersonDate {
  employeeId: number
  name: string
  date: string
}

/** com.ogm.hrms.dto.dashboard.HrDashboardResponse */
export interface HrDashboard {
  totalEmployees: number
  totalDepartments: number
  presentToday: number
  onLeaveToday: number
  pendingLeaveApprovals: number
  pendingWfhApprovals: number
  newJoinersLast30Days: number
  upcomingBirthdays: PersonDate[]
  upcomingAnniversaries: PersonDate[]
  departmentDistribution: CountEntry[]
  genderDistribution: CountEntry[]
}
