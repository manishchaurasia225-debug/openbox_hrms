import { useEffect, useRef, useState } from 'react'
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
import { useDocumentMutations } from '@/features/documents/hooks'
import { documentTypeOptions } from '@/features/documents/constants'
import type { DocumentType } from '@/features/documents/types'

const uploadSchema = z.object({
  documentType: z.string().min(1, 'Select a document type'),
  employeeId: z.string(),
  title: z.string().max(200),
  folder: z.string().max(200),
  description: z.string().max(500),
  expiryDate: z.string(),
})

type UploadFormValues = z.infer<typeof uploadSchema>

const emptyValues: UploadFormValues = {
  documentType: 'RESUME',
  employeeId: '',
  title: '',
  folder: '',
  description: '',
  expiryDate: '',
}

export function UploadDocumentDialog({
  open,
  onOpenChange,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const { upload } = useDocumentMutations()
  const fileRef = useRef<HTMLInputElement>(null)
  const [file, setFile] = useState<File | null>(null)
  const [fileError, setFileError] = useState<string | null>(null)

  const form = useForm<UploadFormValues>({
    resolver: zodResolver(uploadSchema),
    defaultValues: emptyValues,
  })

  useEffect(() => {
    if (open) {
      form.reset(emptyValues)
      setFile(null)
      setFileError(null)
      if (fileRef.current) fileRef.current.value = ''
    }
  }, [open, form])

  const onSubmit = (values: UploadFormValues) => {
    if (!file) {
      setFileError('Choose a file to upload')
      return
    }
    upload.mutate(
      {
        file,
        meta: {
          documentType: values.documentType as DocumentType,
          employeeId: values.employeeId.trim() ? Number(values.employeeId) : undefined,
          title: values.title.trim() || undefined,
          folder: values.folder.trim() || undefined,
          description: values.description.trim() || undefined,
          expiryDate: values.expiryDate.trim() || undefined,
        },
      },
      {
        onSuccess: () => {
          toast.success('Document uploaded')
          onOpenChange(false)
        },
        onError: (error) => {
          if (error instanceof ApiError) {
            error.fieldErrors.forEach((fieldError) =>
              form.setError(fieldError.field as FieldPath<UploadFormValues>, {
                message: fieldError.message,
              }),
            )
            toast.error(error.message)
          } else {
            toast.error('Upload failed')
          }
        },
      },
    )
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Upload document</DialogTitle>
          <DialogDescription>
            Attach a file and its metadata. Link it to an employee by ID, or leave blank for a company
            document.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <FormItem>
              <FormLabel>File</FormLabel>
              <FormControl>
                <Input
                  ref={fileRef}
                  type="file"
                  onChange={(event) => {
                    setFile(event.target.files?.[0] ?? null)
                    setFileError(null)
                  }}
                />
              </FormControl>
              {fileError ? <p className="text-sm text-destructive">{fileError}</p> : null}
            </FormItem>

            <SelectField
              control={form.control}
              name="documentType"
              label="Document type"
              options={documentTypeOptions}
            />
            <div className="grid grid-cols-2 gap-4">
              <TextField
                control={form.control}
                name="employeeId"
                label="Employee ID"
                type="number"
                placeholder="Optional"
              />
              <TextField
                control={form.control}
                name="expiryDate"
                label="Expiry date"
                type="date"
              />
            </div>
            <TextField
              control={form.control}
              name="title"
              label="Title"
              placeholder="Optional — defaults to the file name"
            />
            <TextField
              control={form.control}
              name="folder"
              label="Folder"
              placeholder="Optional — e.g. Onboarding"
            />
            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Description</FormLabel>
                  <FormControl>
                    <Textarea rows={2} placeholder="Optional notes" {...field} />
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
                disabled={upload.isPending}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={upload.isPending}>
                {upload.isPending ? (
                  <>
                    <Loader2 className="mr-2 size-4 animate-spin" /> Uploading…
                  </>
                ) : (
                  'Upload'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
