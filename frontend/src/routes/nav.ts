import type { ComponentType } from 'react'
import type { LucideProps } from 'lucide-react'
import {
  BadgeCheck,
  BarChart3,
  Bell,
  Bot,
  Briefcase,
  Building2,
  CalendarClock,
  CalendarDays,
  CalendarOff,
  FileText,
  LayoutDashboard,
  Megaphone,
  MessageCircle,
  ReceiptText,
  ScrollText,
  Settings,
  ShieldCheck,
  UserCog,
  Users,
  Wallet,
} from 'lucide-react'

export interface NavItem {
  label: string
  to: string
  icon: ComponentType<LucideProps>
  /** Any of these permission codes grants visibility. Empty/omitted = always visible. */
  anyOf?: string[]
}

export interface NavGroup {
  label: string
  items: NavItem[]
}

/**
 * Sidebar navigation. `anyOf` uses backend permission codes (PermissionModule:PermissionAction),
 * so a menu entry only appears when the user could actually use the underlying endpoints.
 */
export const navGroups: NavGroup[] = [
  {
    label: 'Overview',
    items: [{ label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard }],
  },
  {
    label: 'People',
    items: [
      { label: 'Employees', to: '/employees', icon: Users, anyOf: ['EMPLOYEE:VIEW'] },
      { label: 'Attendance', to: '/attendance', icon: CalendarClock, anyOf: ['ATTENDANCE:VIEW'] },
      { label: 'Leave', to: '/leave', icon: CalendarDays, anyOf: ['LEAVE:VIEW'] },
      { label: 'Documents', to: '/documents', icon: FileText, anyOf: ['DOCUMENT:VIEW'] },
    ],
  },
  {
    label: 'Finance',
    items: [
      { label: 'Payroll', to: '/payroll', icon: Wallet, anyOf: ['PAYROLL:VIEW'] },
      { label: 'Reimbursements', to: '/reimbursements', icon: ReceiptText, anyOf: ['EXPENSE:VIEW'] },
    ],
  },
  {
    label: 'Organization',
    items: [
      { label: 'Departments', to: '/departments', icon: Building2, anyOf: ['DEPARTMENT:VIEW'] },
      { label: 'Designations', to: '/designations', icon: Briefcase, anyOf: ['DESIGNATION:VIEW'] },
      { label: 'Employment Types', to: '/employment-types', icon: BadgeCheck, anyOf: ['SETTINGS:VIEW'] },
      { label: 'Holidays', to: '/holidays', icon: CalendarOff, anyOf: ['HOLIDAY:VIEW'] },
    ],
  },
  {
    label: 'Communication',
    items: [
      { label: 'Announcements', to: '/announcements', icon: Megaphone, anyOf: ['ANNOUNCEMENT:VIEW'] },
      { label: 'Notifications', to: '/notifications', icon: Bell, anyOf: ['NOTIFICATION:VIEW'] },
      { label: 'WhatsApp', to: '/whatsapp', icon: MessageCircle, anyOf: ['WHATSAPP:VIEW'] },
    ],
  },
  {
    label: 'Insights',
    items: [
      { label: 'Reports', to: '/reports', icon: BarChart3, anyOf: ['REPORT:VIEW', 'REPORT:EXPORT'] },
      { label: 'AI Assistant', to: '/ai', icon: Bot, anyOf: ['AI:VIEW'] },
    ],
  },
  {
    label: 'Administration',
    items: [
      { label: 'Users', to: '/users', icon: UserCog, anyOf: ['AUTHZ:VIEW'] },
      { label: 'Settings', to: '/settings', icon: Settings, anyOf: ['SETTINGS:VIEW', 'SETTINGS:ADMIN'] },
      { label: 'Audit Logs', to: '/audit', icon: ScrollText, anyOf: ['AUDIT:VIEW'] },
    ],
  },
]

export const brandIcon = ShieldCheck
