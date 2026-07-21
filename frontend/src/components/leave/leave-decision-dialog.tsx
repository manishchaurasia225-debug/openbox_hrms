import { useEffect, useState } from 'react'
import { Loader2 } from 'lucide-react'
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
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { ApiError } from '@/lib/api/client'
import { useLeaveMutations } from '@/features/leave/hooks'
import type { LeaveRequest } from '@/features/leave/types'

export interface LeaveDecision {
  request: LeaveRequest
  action: 'approve' | 'reject'
}

export function LeaveDecisionDialog({
  decision,
  onOpenChange,
}: {
  decision: LeaveDecision | null
  onOpenChange: (open: boolean) => void
}) {
  const { approve, reject } = useLeaveMutations()
  const [remarks, setRemarks] = useState('')

  useEffect(() => {
    setRemarks('')
  }, [decision])

  const open = decision !== null
  const isReject = decision?.action === 'reject'
  const pending = approve.isPending || reject.isPending

  const handleConfirm = () => {
    if (!decision) return
    const mutation = decision.action === 'approve' ? approve : reject
    mutation.mutate(
      { id: decision.request.id, body: { remarks: remarks.trim() || undefined } },
      {
        onSuccess: () => {
          toast.success(isReject ? 'Leave rejected' : 'Leave approved')
          onOpenChange(false)
        },
        onError: (error) =>
          toast.error(error instanceof ApiError ? error.message : 'Action failed'),
      },
    )
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isReject ? 'Reject leave request' : 'Approve leave request'}</DialogTitle>
          <DialogDescription>
            {decision
              ? `${decision.request.employeeName} · ${decision.request.leaveTypeCode} · ${decision.request.days} day(s)`
              : null}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-2">
          <Label htmlFor="leave-remarks">Remarks {isReject ? '' : '(optional)'}</Label>
          <Textarea
            id="leave-remarks"
            rows={3}
            value={remarks}
            onChange={(event) => setRemarks(event.target.value)}
            placeholder={isReject ? 'Reason for rejection' : 'Optional note'}
            maxLength={300}
          />
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            Cancel
          </Button>
          <Button
            variant={isReject ? 'destructive' : 'default'}
            onClick={handleConfirm}
            disabled={pending}
          >
            {pending ? (
              <>
                <Loader2 className="mr-2 size-4 animate-spin" /> Working…
              </>
            ) : isReject ? (
              'Reject'
            ) : (
              'Approve'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
