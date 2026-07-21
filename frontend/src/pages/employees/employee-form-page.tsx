import { useEffect } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, type FieldPath } from 'react-hook-form'
import { ArrowLeft, Loader2 } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Form } from '@/components/ui/form'
import { PageHeader } from '@/components/common/page-header'
import { FormSection, FullWidth } from '@/components/common/form-section'
import { FullPageSpinner } from '@/components/common/full-page-spinner'
import { ErrorState } from '@/components/common/error-state'
import { TextField } from '@/components/form/text-field'
import { SelectField, type SelectOption } from '@/components/form/select-field'
import { ApiError } from '@/lib/api/client'
import {
  useDepartmentOptions,
  useDesignationOptions,
  useEmploymentTypeOptions,
} from '@/features/org/options'
import { useCreateEmployee, useEmployee, useUpdateEmployee } from '@/features/employees/hooks'
import {
  employmentStatusOptions,
  genderOptions,
  maritalStatusOptions,
} from '@/features/employees/constants'
import {
  emptyEmployeeForm,
  employeeFormSchema,
  toFormValues,
  toRequest,
  type EmployeeFormValues,
} from '@/features/employees/form-schema'

function orgToOptions(items: { id: number; name: string }[] | undefined): SelectOption[] {
  return (items ?? []).map((item) => ({ value: String(item.id), label: item.name }))
}

export function EmployeeFormPage() {
  const params = useParams()
  const navigate = useNavigate()
  const employeeId = params.id ? Number(params.id) : undefined
  const isEdit = employeeId != null

  const employeeQuery = useEmployee(employeeId)
  const departments = useDepartmentOptions()
  const designations = useDesignationOptions()
  const employmentTypes = useEmploymentTypeOptions()

  const createEmployee = useCreateEmployee()
  const updateEmployee = useUpdateEmployee(employeeId ?? 0)
  const isSaving = createEmployee.isPending || updateEmployee.isPending

  const form = useForm<EmployeeFormValues>({
    resolver: zodResolver(employeeFormSchema),
    defaultValues: emptyEmployeeForm,
  })

  useEffect(() => {
    if (isEdit && employeeQuery.data) {
      form.reset(toFormValues(employeeQuery.data))
    }
  }, [isEdit, employeeQuery.data, form])

  const onSubmit = (values: EmployeeFormValues) => {
    const body = toRequest(values)
    const handlers = {
      onSuccess: (saved: { id: number }) => {
        toast.success(isEdit ? 'Employee updated' : 'Employee created')
        navigate(`/employees/${saved.id}`)
      },
      onError: (error: unknown) => {
        if (error instanceof ApiError) {
          error.fieldErrors.forEach((fieldError) => {
            form.setError(fieldError.field as FieldPath<EmployeeFormValues>, {
              message: fieldError.message,
            })
          })
          toast.error(error.message)
        } else {
          toast.error('Failed to save employee')
        }
      },
    }
    if (isEdit) {
      updateEmployee.mutate(body, handlers)
    } else {
      createEmployee.mutate(body, handlers)
    }
  }

  if (isEdit && employeeQuery.isLoading) {
    return <FullPageSpinner />
  }
  if (isEdit && employeeQuery.isError) {
    return <ErrorState error={employeeQuery.error} onRetry={() => employeeQuery.refetch()} />
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)} aria-label="Back">
          <ArrowLeft className="size-5" />
        </Button>
        <PageHeader
          title={isEdit ? 'Edit employee' : 'New employee'}
          description={
            isEdit ? 'Update the employee master record.' : 'Create a new employee master record.'
          }
        />
      </div>

      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6" noValidate>
          <FormSection title="Basic information" description="Employee code and full name are required.">
            <TextField control={form.control} name="employeeCode" label="Employee code" placeholder="EMP-0001" />
            <TextField control={form.control} name="fullName" label="Full name" placeholder="Jane Doe" />
            <SelectField control={form.control} name="gender" label="Gender" options={genderOptions} />
            <TextField control={form.control} name="dateOfBirth" label="Date of birth" type="date" />
            <TextField control={form.control} name="bloodGroup" label="Blood group" placeholder="O+" />
            <TextField control={form.control} name="nationality" label="Nationality" placeholder="Indian" />
            <SelectField control={form.control} name="maritalStatus" label="Marital status" options={maritalStatusOptions} />
            <TextField control={form.control} name="photoUrl" label="Photo URL" type="url" placeholder="https://…" />
          </FormSection>

          <FormSection title="Contact">
            <TextField control={form.control} name="contact.mobile" label="Mobile" type="tel" />
            <TextField control={form.control} name="contact.personalEmail" label="Personal email" type="email" />
            <TextField control={form.control} name="contact.officialEmail" label="Official email" type="email" />
            <div className="hidden sm:block" aria-hidden />
            <FullWidth>
              <TextField control={form.control} name="contact.currentAddress" label="Current address" />
            </FullWidth>
            <FullWidth>
              <TextField control={form.control} name="contact.permanentAddress" label="Permanent address" />
            </FullWidth>
          </FormSection>

          <FormSection title="Employment">
            <SelectField
              control={form.control}
              name="employment.departmentId"
              label="Department"
              options={orgToOptions(departments.data)}
              disabled={departments.isLoading}
              placeholder={departments.isLoading ? 'Loading…' : 'Select department'}
            />
            <SelectField
              control={form.control}
              name="employment.designationId"
              label="Designation"
              options={orgToOptions(designations.data)}
              disabled={designations.isLoading}
              placeholder={designations.isLoading ? 'Loading…' : 'Select designation'}
            />
            <SelectField
              control={form.control}
              name="employment.employmentTypeId"
              label="Employment type"
              options={orgToOptions(employmentTypes.data)}
              disabled={employmentTypes.isLoading}
              placeholder={employmentTypes.isLoading ? 'Loading…' : 'Select type'}
            />
            <SelectField
              control={form.control}
              name="employment.employmentStatus"
              label="Employment status"
              options={employmentStatusOptions}
            />
            <TextField control={form.control} name="employment.joiningDate" label="Joining date" type="date" />
            <TextField control={form.control} name="employment.endDate" label="End date" type="date" />
            <TextField
              control={form.control}
              name="employment.noticePeriodDays"
              label="Notice period (days)"
              type="number"
            />
          </FormSection>

          <FormSection title="Salary" description="Monthly amounts.">
            <TextField control={form.control} name="salary.basicSalary" label="Basic salary" type="number" />
            <TextField control={form.control} name="salary.hra" label="HRA" type="number" />
            <TextField control={form.control} name="salary.specialAllowance" label="Special allowance" type="number" />
            <TextField control={form.control} name="salary.bonus" label="Bonus" type="number" />
            <TextField control={form.control} name="salary.incentives" label="Incentives" type="number" />
            <TextField control={form.control} name="salary.otherAllowances" label="Other allowances" type="number" />
          </FormSection>

          <FormSection title="Bank details">
            <TextField control={form.control} name="bank.bankName" label="Bank name" />
            <TextField control={form.control} name="bank.accountHolderName" label="Account holder name" />
            <TextField control={form.control} name="bank.accountNumber" label="Account number" />
            <TextField control={form.control} name="bank.ifscCode" label="IFSC code" />
          </FormSection>

          <FormSection title="Government IDs">
            <TextField control={form.control} name="governmentIds.pan" label="PAN" />
            <TextField control={form.control} name="governmentIds.aadhaar" label="Aadhaar" />
            <TextField control={form.control} name="governmentIds.passport" label="Passport" />
            <TextField control={form.control} name="governmentIds.drivingLicense" label="Driving license" />
          </FormSection>

          <FormSection title="Social profiles">
            <TextField control={form.control} name="social.linkedinUrl" label="LinkedIn" type="url" />
            <TextField control={form.control} name="social.xUrl" label="X (Twitter)" type="url" />
            <TextField control={form.control} name="social.instagramUrl" label="Instagram" type="url" />
            <TextField control={form.control} name="social.facebookUrl" label="Facebook" type="url" />
          </FormSection>

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => navigate(-1)} disabled={isSaving}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSaving}>
              {isSaving ? (
                <>
                  <Loader2 className="mr-2 size-4 animate-spin" /> Saving…
                </>
              ) : isEdit ? (
                'Save changes'
              ) : (
                'Create employee'
              )}
            </Button>
          </div>
        </form>
      </Form>
    </div>
  )
}
