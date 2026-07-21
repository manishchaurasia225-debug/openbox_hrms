import type { ReactNode } from 'react'
import { ArrowLeft, Pencil } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageHeader } from '@/components/common/page-header'
import { FullPageSpinner } from '@/components/common/full-page-spinner'
import { ErrorState } from '@/components/common/error-state'
import { Can } from '@/components/auth/require-permission'
import { formatDate, formatDateTime, orDash } from '@/lib/format'
import { useEmployee } from '@/features/employees/hooks'
import {
  employmentStatusLabels,
  employmentStatusVariant,
  genderLabels,
  maritalStatusLabels,
} from '@/features/employees/constants'

function DetailRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex flex-col gap-0.5">
      <dt className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</dt>
      <dd className="text-sm">{children}</dd>
    </div>
  )
}

function DetailCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <dl className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">{children}</dl>
      </CardContent>
    </Card>
  )
}

function money(value?: number): string {
  if (value == null) return '—'
  return new Intl.NumberFormat('en-IN').format(value)
}

export function EmployeeDetailPage() {
  const params = useParams()
  const navigate = useNavigate()
  const employeeId = params.id ? Number(params.id) : undefined
  const { data: employee, isLoading, isError, error, refetch } = useEmployee(employeeId)

  if (isLoading) return <FullPageSpinner />
  if (isError) return <ErrorState error={error} onRetry={() => refetch()} />
  if (!employee) return null

  const status = employee.employment?.employmentStatus

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" onClick={() => navigate('/employees')} aria-label="Back">
          <ArrowLeft className="size-5" />
        </Button>
        <PageHeader
          title={employee.fullName}
          description={employee.employeeCode}
          actions={
            <Can anyOf={['EMPLOYEE:EDIT']}>
              <Button onClick={() => navigate(`/employees/${employee.id}/edit`)}>
                <Pencil className="mr-2 size-4" /> Edit
              </Button>
            </Can>
          }
        />
      </div>

      <DetailCard title="Basic information">
        <DetailRow label="Employee code">{employee.employeeCode}</DetailRow>
        <DetailRow label="Full name">{employee.fullName}</DetailRow>
        <DetailRow label="Status">
          {status ? (
            <Badge variant={employmentStatusVariant[status]}>{employmentStatusLabels[status]}</Badge>
          ) : (
            '—'
          )}
        </DetailRow>
        <DetailRow label="Gender">{employee.gender ? genderLabels[employee.gender] : '—'}</DetailRow>
        <DetailRow label="Date of birth">{formatDate(employee.dateOfBirth)}</DetailRow>
        <DetailRow label="Blood group">{orDash(employee.bloodGroup)}</DetailRow>
        <DetailRow label="Nationality">{orDash(employee.nationality)}</DetailRow>
        <DetailRow label="Marital status">
          {employee.maritalStatus ? maritalStatusLabels[employee.maritalStatus] : '—'}
        </DetailRow>
      </DetailCard>

      <DetailCard title="Contact">
        <DetailRow label="Mobile">{orDash(employee.contact?.mobile)}</DetailRow>
        <DetailRow label="Personal email">{orDash(employee.contact?.personalEmail)}</DetailRow>
        <DetailRow label="Official email">{orDash(employee.contact?.officialEmail)}</DetailRow>
        <DetailRow label="Current address">{orDash(employee.contact?.currentAddress)}</DetailRow>
        <DetailRow label="Permanent address">{orDash(employee.contact?.permanentAddress)}</DetailRow>
      </DetailCard>

      <DetailCard title="Employment">
        <DetailRow label="Department">{orDash(employee.employment?.departmentName)}</DetailRow>
        <DetailRow label="Designation">{orDash(employee.employment?.designationName)}</DetailRow>
        <DetailRow label="Employment type">{orDash(employee.employment?.employmentTypeName)}</DetailRow>
        <DetailRow label="Joining date">{formatDate(employee.employment?.joiningDate)}</DetailRow>
        <DetailRow label="End date">{formatDate(employee.employment?.endDate)}</DetailRow>
        <DetailRow label="Notice period">
          {employee.employment?.noticePeriodDays != null
            ? `${employee.employment.noticePeriodDays} days`
            : '—'}
        </DetailRow>
      </DetailCard>

      <DetailCard title="Salary">
        <DetailRow label="Basic">{money(employee.salary?.basicSalary)}</DetailRow>
        <DetailRow label="HRA">{money(employee.salary?.hra)}</DetailRow>
        <DetailRow label="Special allowance">{money(employee.salary?.specialAllowance)}</DetailRow>
        <DetailRow label="Bonus">{money(employee.salary?.bonus)}</DetailRow>
        <DetailRow label="Incentives">{money(employee.salary?.incentives)}</DetailRow>
        <DetailRow label="Other allowances">{money(employee.salary?.otherAllowances)}</DetailRow>
      </DetailCard>

      <DetailCard title="Bank details">
        <DetailRow label="Bank name">{orDash(employee.bank?.bankName)}</DetailRow>
        <DetailRow label="Account holder">{orDash(employee.bank?.accountHolderName)}</DetailRow>
        <DetailRow label="Account number">{orDash(employee.bank?.accountNumber)}</DetailRow>
        <DetailRow label="IFSC">{orDash(employee.bank?.ifscCode)}</DetailRow>
      </DetailCard>

      <DetailCard title="Government IDs">
        <DetailRow label="PAN">{orDash(employee.governmentIds?.pan)}</DetailRow>
        <DetailRow label="Aadhaar">{orDash(employee.governmentIds?.aadhaar)}</DetailRow>
        <DetailRow label="Passport">{orDash(employee.governmentIds?.passport)}</DetailRow>
        <DetailRow label="Driving license">{orDash(employee.governmentIds?.drivingLicense)}</DetailRow>
      </DetailCard>

      <DetailCard title="Social profiles">
        <DetailRow label="LinkedIn">{orDash(employee.social?.linkedinUrl)}</DetailRow>
        <DetailRow label="X (Twitter)">{orDash(employee.social?.xUrl)}</DetailRow>
        <DetailRow label="Instagram">{orDash(employee.social?.instagramUrl)}</DetailRow>
        <DetailRow label="Facebook">{orDash(employee.social?.facebookUrl)}</DetailRow>
      </DetailCard>

      <DetailCard title="Record">
        <DetailRow label="Created">{formatDateTime(employee.createdAt)}</DetailRow>
        <DetailRow label="Updated">{formatDateTime(employee.updatedAt)}</DetailRow>
      </DetailCard>
    </div>
  )
}
