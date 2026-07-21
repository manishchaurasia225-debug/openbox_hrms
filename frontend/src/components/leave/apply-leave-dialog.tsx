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
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { TextField } from '@/components/form/text-field'
import { SelectField } from '@/components/form/select-field'
import { ApiError } from '@/lib/api/client'
import { useLeaveMutations, useLeaveTypes } from '@/features/leave/hooks'

const applySchema = z
  .object({
    leaveTypeId: z.string().min(1, 'Select a leave type'),
    fromDate: z.string().min(1, 'From date is required'),
    toDate: z.string().min(1, 'To date is required'),
    halfDay: z.boolean(),
    reason: z.string().max(500),
  })
  .refine((values) => !values.fromDate || !values.toDate || values.fromDate <= values.toDate, {
    path: ['toDate'],
    message: 'To date cannot be before the from date',
  })

type ApplyFormValues = z.infer<typeof applySchema>

const emptyValues: ApplyFormValues = {
  leaveTypeId: '',
  fromDate: '',
  toDate: '',
  halfDay: false,
  reason: '',
}

export function ApplyLeaveDialog({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const leaveTypes = useLeaveTypes()
  const { apply } = useLeaveMutations()

  const form = useForm<ApplyFormValues>({
    resolver: zodResolver(applySchema),
    defaultValues: emptyValues,
  })

  useEffect(() => {
    if (open) form.reset(emptyValues)
  }, [open, form])

  const typeOptions = (leaveTypes.data ?? [])
    .filter((type) => type.active)
    .map((type) => ({ value: String(type.id), label: `${type.name} (${type.code})` }))

  const onSubmit = (values: ApplyFormValues) => {
    apply.mutate(
      {
        leaveTypeId: Number(values.leaveTypeId),
        fromDate: values.fromDate,
        toDate: values.toDate,
        halfDay: values.halfDay,
        reason: values.reason.trim() || undefined,
      },
      {
        onSuccess: () => {
          toast.success('Leave request submitted')
          onOpenChange(false)
        },
        onError: (error) => {
          if (error instanceof ApiError) {
            error.fieldErrors.forEach((fieldError) =>
              form.setError(fieldError.field as FieldPath<ApplyFormValues>, {
                message: fieldError.message,
              }),
            )
            toast.error(error.message)
          } else {
            toast.error('Failed to submit leave request')
          }
        },
      },
    )
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Apply for leave</DialogTitle>
          <DialogDescription>Submit a leave request for approval.</DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <SelectField
              control={form.control}
              name="leaveTypeId"
              label="Leave type"
              options={typeOptions}
              placeholder={leaveTypes.isLoading ? 'Loading…' : 'Select leave type'}
              disabled={leaveTypes.isLoading}
            />
            <div className="grid grid-cols-2 gap-4">
              <TextField control={form.control} name="fromDate" label="From" type="date" />
              <TextField control={form.control} name="toDate" label="To" type="date" />
            </div>
            <FormField
              control={form.control}
              name="halfDay"
              render={({ field }) => (
                <FormItem className="flex items-center justify-between rounded-lg border p-3">
                  <div className="space-y-0.5">
                    <FormLabel>Half day</FormLabel>
                    <FormDescription>Applies to a single-day request.</FormDescription>
                  </div>
                  <FormControl>
                    <Switch checked={field.value} onCheckedChange={field.onChange} />
                  </FormControl>
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="reason"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Reason</FormLabel>
                  <FormControl>
                    <Textarea rows={3} placeholder="Optional reason" {...field} />
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
                disabled={apply.isPending}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={apply.isPending}>
                {apply.isPending ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" /> Submitting…
                  </>
                ) : (
                  'Submit request'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
