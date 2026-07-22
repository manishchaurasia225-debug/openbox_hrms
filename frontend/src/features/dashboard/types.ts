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

/** com.ogm.hrms.dto.attendance.AttendanceSummaryResponse (subset used on the dashboard). */
export interface AttendanceMonthSummary {
  year: number
  month: number
  presentDays: number
  leaveDays: number
  absentDays: number
  totalDays: number
  totalWorkingMinutes: number
}

/** com.ogm.hrms.dto.leave.LeaveBalanceResponse */
export interface LeaveBalanceItem {
  id: number
  leaveTypeCode: string
  year: number
  allocated: number
  used: number
  remaining: number
}

/** com.ogm.hrms.dto.holiday.HolidayResponse (subset). */
export interface HolidayItem {
  id: number
  holidayDate: string
  name: string
  region?: string
}

/** com.ogm.hrms.dto.communication.AnnouncementResponse (subset). */
export interface AnnouncementItem {
  id: number
  title: string
  body: string
  publishAt?: string
  pinned: boolean
}

/** com.ogm.hrms.dto.dashboard.EmployeeDashboardResponse */
export interface EmployeeDashboard {
  employeeId: number
  employeeName: string
  employeeCode: string
  department?: string
  designation?: string
  profileCompletionPercent: number
  attendanceThisMonth: AttendanceMonthSummary
  leaveBalances: LeaveBalanceItem[]
  upcomingHolidays: HolidayItem[]
  recentAnnouncements: AnnouncementItem[]
  unreadNotifications: number
  payslipCount: number
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
