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
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { ApiError } from '@/lib/api/client'
import { useMasterMutations } from '@/features/masters/hooks'
import type { MasterConfig } from '@/features/masters/config'
import type { MasterRecord } from '@/features/masters/types'

const masterFormSchema = z.object({
  code: z.string().trim().min(1, 'Code is required').max(40),
  name: z.string().trim().min(1, 'Name is required').max(120),
  description: z.string().max(255),
  active: z.boolean(),
})

type MasterFormValues = z.infer<typeof masterFormSchema>

const emptyValues: MasterFormValues = { code: '', name: '', description: '', active: true }

interface MasterFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  config: MasterConfig
  /** null = create, a record = edit. */
  record: MasterRecord | null
}

export function MasterFormDialog({ open, onOpenChange, config, record }: MasterFormDialogProps) {
  const { create, update } = useMasterMutations(config)
  const isEdit = record !== null
  const saving = create.isPending || update.isPending

  const form = useForm<MasterFormValues>({
    resolver: zodResolver(masterFormSchema),
    defaultValues: emptyValues,
  })

  // Reset the form whenever the dialog opens for a different record.
  useEffect(() => {
    if (!open) return
    form.reset(
      record
        ? {
            code: record.code,
            name: record.name,
            description: record.description ?? '',
            active: record.active,
          }
        : emptyValues,
    )
  }, [open, record, form])

  const onSubmit = (values: MasterFormValues) => {
    const body = {
      code: values.code.trim(),
      name: values.name.trim(),
      description: values.description.trim() || undefined,
      active: values.active,
    }
    const handlers = {
      onSuccess: () => {
        toast.success(isEdit ? `${config.singular} updated` : `${config.singular} created`)
        onOpenChange(false)
      },
      onError: (error: unknown) => {
        if (error instanceof ApiError) {
          error.fieldErrors.forEach((fieldError) =>
            form.setError(fieldError.field as FieldPath<MasterFormValues>, {
              message: fieldError.message,
            }),
          )
          toast.error(error.message)
        } else {
          toast.error('Save failed')
        }
      },
    }
    if (record) {
      update.mutate({ id: record.id, body }, handlers)
    } else {
      create.mutate(body, handlers)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {isEdit ? `Edit ${config.singular}` : `New ${config.singular}`}
          </DialogTitle>
          <DialogDescription>
            Code and name are required. Codes are unique per {config.singular}.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <FormField
              control={form.control}
              name="code"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Code</FormLabel>
                  <FormControl>
                    <Input placeholder="ENG" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input placeholder="Engineering" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
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
            <FormField
              control={form.control}
              name="active"
              render={({ field }) => (
                <FormItem className="flex items-center justify-between rounded-lg border p-3">
                  <div className="space-y-0.5">
                    <FormLabel>Active</FormLabel>
                    <FormDescription>Inactive records are hidden from selection.</FormDescription>
                  </div>
                  <FormControl>
                    <Switch checked={field.value} onCheckedChange={field.onChange} />
                  </FormControl>
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
                disabled={saving}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={saving}>
                {saving ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" /> Saving…
                  </>
                ) : isEdit ? (
                  'Save changes'
                ) : (
                  'Create'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
