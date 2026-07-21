import { createBrowserRouter, Navigate } from 'react-router-dom'
import { ProtectedRoute } from '@/components/auth/protected-route'
import { GuestRoute } from '@/components/auth/guest-route'
import { RequirePermission } from '@/components/auth/require-permission'
import { AppLayout } from '@/layouts/app-layout'
import { AuthLayout } from '@/layouts/auth-layout'
import { LoginPage } from '@/pages/login-page'
import { DashboardPage } from '@/pages/dashboard-page'
import { PlaceholderPage } from '@/pages/placeholder-page'
import { ForbiddenPage } from '@/pages/forbidden-page'
import { NotFoundPage } from '@/pages/not-found-page'
import { EmployeesListPage } from '@/pages/employees/employees-list-page'
import { EmployeeFormPage } from '@/pages/employees/employee-form-page'
import { EmployeeDetailPage } from '@/pages/employees/employee-detail-page'
import { DepartmentsPage } from '@/pages/masters/departments-page'
import { DesignationsPage } from '@/pages/masters/designations-page'
import { EmploymentTypesPage } from '@/pages/masters/employment-types-page'
import { LeavePage } from '@/pages/leave/leave-page'
import { AttendancePage } from '@/pages/attendance/attendance-page'
import { ReimbursementsPage } from '@/pages/reimbursements/reimbursements-page'

/**
 * Feature routes. Each is permission-gated and currently renders a placeholder;
 * the route + layout + RBAC wiring is complete so feature pages drop straight in.
 */
interface FeatureRoute {
  path: string
  title: string
  anyOf: string[]
}

const featureRoutes: FeatureRoute[] = [
  { path: 'documents', title: 'Documents', anyOf: ['DOCUMENT:VIEW'] },
  { path: 'payroll', title: 'Payroll', anyOf: ['PAYROLL:VIEW'] },
  { path: 'holidays', title: 'Holidays', anyOf: ['HOLIDAY:VIEW'] },
  { path: 'announcements', title: 'Announcements', anyOf: ['ANNOUNCEMENT:VIEW'] },
  { path: 'notifications', title: 'Notifications', anyOf: ['NOTIFICATION:VIEW'] },
  { path: 'whatsapp', title: 'WhatsApp', anyOf: ['WHATSAPP:VIEW'] },
  { path: 'reports', title: 'Reports', anyOf: ['REPORT:VIEW', 'REPORT:EXPORT'] },
  { path: 'ai', title: 'AI Assistant', anyOf: ['AI:VIEW'] },
  { path: 'users', title: 'Users', anyOf: ['AUTHZ:VIEW'] },
  { path: 'settings', title: 'Settings', anyOf: ['SETTINGS:VIEW', 'SETTINGS:ADMIN'] },
  { path: 'audit', title: 'Audit Logs', anyOf: ['AUDIT:VIEW'] },
]

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <GuestRoute />,
    children: [
      {
        element: <AuthLayout />,
        children: [{ index: true, element: <LoginPage /> }],
      },
    ],
  },
  {
    path: '/',
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: 'dashboard', element: <DashboardPage /> },
          { path: '403', element: <ForbiddenPage /> },
          {
            path: 'employees',
            element: <RequirePermission anyOf={['EMPLOYEE:VIEW']} />,
            children: [
              { index: true, element: <EmployeesListPage /> },
              {
                path: 'new',
                element: <RequirePermission anyOf={['EMPLOYEE:CREATE']} />,
                children: [{ index: true, element: <EmployeeFormPage /> }],
              },
              { path: ':id', element: <EmployeeDetailPage /> },
              {
                path: ':id/edit',
                element: <RequirePermission anyOf={['EMPLOYEE:EDIT']} />,
                children: [{ index: true, element: <EmployeeFormPage /> }],
              },
            ],
          },
          {
            path: 'departments',
            element: <RequirePermission anyOf={['DEPARTMENT:VIEW']} />,
            children: [{ index: true, element: <DepartmentsPage /> }],
          },
          {
            path: 'designations',
            element: <RequirePermission anyOf={['DESIGNATION:VIEW']} />,
            children: [{ index: true, element: <DesignationsPage /> }],
          },
          {
            path: 'employment-types',
            element: <RequirePermission anyOf={['SETTINGS:VIEW']} />,
            children: [{ index: true, element: <EmploymentTypesPage /> }],
          },
          {
            path: 'attendance',
            element: <RequirePermission anyOf={['ATTENDANCE:VIEW']} />,
            children: [{ index: true, element: <AttendancePage /> }],
          },
          {
            path: 'leave',
            element: <RequirePermission anyOf={['LEAVE:VIEW']} />,
            children: [{ index: true, element: <LeavePage /> }],
          },
          {
            path: 'reimbursements',
            element: <RequirePermission anyOf={['EXPENSE:VIEW']} />,
            children: [{ index: true, element: <ReimbursementsPage /> }],
          },
          ...featureRoutes.map((route) => ({
            path: route.path,
            element: <RequirePermission anyOf={route.anyOf} />,
            children: [{ index: true, element: <PlaceholderPage title={route.title} /> }],
          })),
        ],
      },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
])
