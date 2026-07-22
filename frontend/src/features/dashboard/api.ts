import { http } from '@/lib/api/client'
import type { EmployeeDashboard, HrDashboard } from './types'

/** com.ogm.hrms.controller.DashboardController */
export const dashboardApi = {
  hr: () => http.get<HrDashboard>('/dashboard/hr'),
  me: () => http.get<EmployeeDashboard>('/dashboard/me'),
}
