import { useEffect } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, type FieldPath } from 'react-hook-form'
import { Loader2 } from 'lucide-react'
import { z } from 'zod'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { TextField } from '@/components/form/text-field'
import { SelectField } from '@/components/form/select-field'
import { ApiError } from '@/lib/api/client'
import { useAttendanceMutations } from '@/features/attendance/hooks'
import { attendanceTypeOptions } from '@/features/attendance/constants'
import type { AttendanceRecord, AttendanceType } from '@/features/attendance/types'

const correctionSchema = z.object({
  employeeId: z.string().min(1, 'Employee ID is required'),
  date: z.string().min(1, 'Date is required'),
  attendanceType: z.string().min(1, 'Select an attendance type'),
  clockIn: z.string(),
  clockOut: z.string(),
  remarks: z.string().max(300),
})

type CorrectionFormValues = z.infer<typeof correctionSchema>

/** Convert a browser "datetime-local" value (no offset) to an ISO-8601 instant, or undefined. */
function toInstant(value: string): string | undefined {
  if (!value.trim()) return undefined
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? undefined : parsed.toISOString()
}

const blankValues: CorrectionFormValues = {
  employeeId: '',
  date: '',
  attendanceType: 'OFFICE',
  clockIn: '',
  clockOut: '',
  remarks: '',
}

export function AttendanceCorrectionDialog({
  open,
  record,
  onOpenChange,
}: {
  open: boolean
  /** When set, pre-fill from this record; otherwise open a blank regularization form. */
  record?: AttendanceRecord | null
  onOpenChange: (open: boolean) => void
}) {
  const { correct } = useAttendanceMutations()

  const form = useForm<CorrectionFormValues>({
    resolver: zodResolver(correctionSchema),
    defaultValues: blankValues,
  })

  useEffect(() => {
    if (!open) return
    form.reset(
      record
        ? {
            employeeId: String(record.employeeId),
            date: record.attendanceDate,
            attendanceType: record.attendanceType,
            clockIn: '',
            clockOut: '',
            remarks: record.remarks ?? '',
          }
        : blankValues,
    )
  }, [open, record, form])

  const onSubmit = (values: CorrectionFormValues) => {
    correct.mutate(
      {
        employeeId: Number(values.employeeId),
        date: values.date,
        attendanceType: values.attendanceType as AttendanceType,
        clockIn: toInstant(values.clockIn),
        clockOut: toInstant(values.clockOut),
        remarks: values.remarks.trim() || undefined,
      },
      {
        onSuccess: () => {
          toast.success('Attendance corrected')
          onOpenChange(false)
        },
        onError: (error) => {
          if (error instanceof ApiError) {
            error.fieldErrors.forEach((fieldError) =>
              form.setError(fieldError.field as FieldPath<CorrectionFormValues>, {
                message: fieldError.message,
              }),
            )
            toast.error(error.message)
          } else {
            toast.error('Correction failed')
          }
        },
      },
    )
  }

  return (
    <Dialog open={open} onOpenChange={(next) => !next && onOpenChange(false)}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Correct attendance</DialogTitle>
          <DialogDescription>
            Regularize an employee's attendance for a specific date. Leave clock times empty to keep
            them unchanged.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <div className="grid grid-cols-2 gap-4">
              <TextField
                control={form.control}
                name="employeeId"
                label="Employee ID"
                type="number"
              />
              <TextField control={form.control} name="date" label="Date" type="date" />
            </div>
            <SelectField
              control={form.control}
              name="attendanceType"
              label="Attendance type"
              options={attendanceTypeOptions}
            />
            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="clockIn"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Clock in</FormLabel>
                    <FormControl>
                      <Input type="datetime-local" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="clockOut"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Clock out</FormLabel>
                    <FormControl>
                      <Input type="datetime-local" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <FormField
              control={form.control}
              name="remarks"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Remarks</FormLabel>
                  <FormControl>
                    <Textarea rows={2} placeholder="Reason for the correction" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
                disabled={correct.isPending}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={correct.isPending}>
                {correct.isPending ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" /> Saving…
                  </>
                ) : (
                  'Save correction'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
