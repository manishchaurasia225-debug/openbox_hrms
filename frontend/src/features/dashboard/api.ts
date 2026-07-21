import { http } from '@/lib/api/client'
import type { HrDashboard } from './types'

/** com.ogm.hrms.controller.DashboardController */
export const dashboardApi = {
  hr: () => http.get<HrDashboard>('/dashboard/hr'),
}
