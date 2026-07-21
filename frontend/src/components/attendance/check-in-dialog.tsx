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
import { Textarea } from '@/components/ui/textarea'
import { TextField } from '@/components/form/text-field'
import { SelectField } from '@/components/form/select-field'
import { ApiError } from '@/lib/api/client'
import { useAttendanceMutations } from '@/features/attendance/hooks'
import { checkInModeOptions } from '@/features/attendance/constants'
import type { CheckInMode } from '@/features/attendance/types'

const checkInSchema = z
  .object({
    mode: z.enum(['OFFICE', 'WORK_FROM_HOME']),
    wfhReason: z.string().max(300),
    workLocation: z.string().max(150),
    expectedHours: z.string(),
  })
  .refine((values) => values.mode !== 'WORK_FROM_HOME' || values.wfhReason.trim().length > 0, {
    path: ['wfhReason'],
    message: 'A reason is required when working from home',
  })

type CheckInFormValues = z.infer<typeof checkInSchema>

const emptyValues: CheckInFormValues = {
  mode: 'OFFICE',
  wfhReason: '',
  workLocation: '',
  expectedHours: '',
}

export function CheckInDialog({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const { checkIn } = useAttendanceMutations()

  const form = useForm<CheckInFormValues>({
    resolver: zodResolver(checkInSchema),
    defaultValues: emptyValues,
  })

  useEffect(() => {
    if (open) form.reset(emptyValues)
  }, [open, form])

  const mode = form.watch('mode') as CheckInMode
  const isWfh = mode === 'WORK_FROM_HOME'

  const onSubmit = (values: CheckInFormValues) => {
    const expectedHours = values.expectedHours.trim()
    checkIn.mutate(
      {
        mode: values.mode,
        wfhReason: isWfh ? values.wfhReason.trim() || undefined : undefined,
        workLocation: values.workLocation.trim() || undefined,
        expectedHours: expectedHours ? Number(expectedHours) : undefined,
      },
      {
        onSuccess: () => {
          toast.success('Checked in')
          onOpenChange(false)
        },
        onError: (error) => {
          if (error instanceof ApiError) {
            error.fieldErrors.forEach((fieldError) =>
              form.setError(fieldError.field as FieldPath<CheckInFormValues>, {
                message: fieldError.message,
              }),
            )
            toast.error(error.message)
          } else {
            toast.error('Check-in failed')
          }
        },
      },
    )
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Check in</DialogTitle>
          <DialogDescription>
            Record your check-in for today. Office check-in is only accepted from the office network.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <SelectField
              control={form.control}
              name="mode"
              label="Mode"
              options={checkInModeOptions}
              placeholder="Select mode"
            />
            {isWfh ? (
              <FormField
                control={form.control}
                name="wfhReason"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Work-from-home reason</FormLabel>
                    <FormControl>
                      <Textarea rows={3} placeholder="Why are you working from home?" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            ) : null}
            <TextField
              control={form.control}
              name="workLocation"
              label="Work location"
              placeholder="Optional — e.g. Client site, HQ 3rd floor"
            />
            <TextField
              control={form.control}
              name="expectedHours"
              label="Expected hours"
              type="number"
              placeholder="Optional"
            />

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
                disabled={checkIn.isPending}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={checkIn.isPending}>
                {checkIn.isPending ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" /> Checking in…
                  </>
                ) : (
                  'Check in'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
