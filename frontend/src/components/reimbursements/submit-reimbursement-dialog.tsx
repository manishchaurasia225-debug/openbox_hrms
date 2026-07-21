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
import { useReimbursementMutations } from '@/features/reimbursements/hooks'
import { expenseCategoryOptions } from '@/features/reimbursements/constants'
import type { ExpenseCategory } from '@/features/reimbursements/types'

const submitSchema = z.object({
  category: z.string().min(1, 'Select a category'),
  amount: z
    .string()
    .min(1, 'Amount is required')
    .refine((value) => Number(value) > 0, 'Amount must be greater than 0'),
  expenseDate: z.string().min(1, 'Expense date is required'),
  description: z.string().max(500),
})

type SubmitFormValues = z.infer<typeof submitSchema>

const emptyValues: SubmitFormValues = {
  category: '',
  amount: '',
  expenseDate: '',
  description: '',
}

export function SubmitReimbursementDialog({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const { submit } = useReimbursementMutations()
  const form = useForm<SubmitFormValues>({
    resolver: zodResolver(submitSchema),
    defaultValues: emptyValues,
  })

  useEffect(() => {
    if (open) form.reset(emptyValues)
  }, [open, form])

  const onSubmit = (values: SubmitFormValues) => {
    submit.mutate(
      {
        category: values.category as ExpenseCategory,
        amount: Number(values.amount),
        expenseDate: values.expenseDate,
        description: values.description.trim() || undefined,
      },
      {
        onSuccess: () => {
          toast.success('Reimbursement claim submitted')
          onOpenChange(false)
        },
        onError: (error) => {
          if (error instanceof ApiError) {
            error.fieldErrors.forEach((fieldError) =>
              form.setError(fieldError.field as FieldPath<SubmitFormValues>, {
                message: fieldError.message,
              }),
            )
            toast.error(error.message)
          } else {
            toast.error('Failed to submit claim')
          }
        },
      },
    )
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>New reimbursement claim</DialogTitle>
          <DialogDescription>Submit an expense for approval.</DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <SelectField
              control={form.control}
              name="category"
              label="Category"
              options={expenseCategoryOptions}
              placeholder="Select category"
            />
            <div className="grid grid-cols-2 gap-4">
              <TextField control={form.control} name="amount" label="Amount" type="number" />
              <TextField control={form.control} name="expenseDate" label="Expense date" type="date" />
            </div>
            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Description</FormLabel>
                  <FormControl>
                    <Textarea rows={3} placeholder="Optional description" {...field} />
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
                disabled={submit.isPending}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={submit.isPending}>
                {submit.isPending ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" /> Submitting…
                  </>
                ) : (
                  'Submit claim'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
