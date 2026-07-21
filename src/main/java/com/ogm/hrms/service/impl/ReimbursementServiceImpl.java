package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.reimbursement.ReimbursementResponse;
import com.ogm.hrms.dto.reimbursement.SubmitReimbursementRequest;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.ReimbursementClaim;
import com.ogm.hrms.enums.NotificationChannel;
import com.ogm.hrms.enums.ReimbursementStatus;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.DocumentRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.ReimbursementClaimRepository;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.NotificationService;
import com.ogm.hrms.service.ReimbursementService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Default {@link ReimbursementService}. Two-level approval (manager → finance) then payout; an
 * employee cannot approve their own claim (business rule); decisions notify the employee in-app.
 */
@Service
public class ReimbursementServiceImpl implements ReimbursementService {

    private final ReimbursementClaimRepository claimRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;
    private final NotificationService notificationService;

    public ReimbursementServiceImpl(ReimbursementClaimRepository claimRepository,
                                    EmployeeRepository employeeRepository, DocumentRepository documentRepository,
                                    NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.employeeRepository = employeeRepository;
        this.documentRepository = documentRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ReimbursementResponse submit(AuthenticatedUser principal, SubmitReimbursementRequest r) {
        Employee employee = currentEmployee(principal);
        ReimbursementClaim claim = new ReimbursementClaim();
        claim.setEmployee(employee);
        claim.setCategory(r.category());
        claim.setAmount(r.amount());
        claim.setExpenseDate(r.expenseDate());
        claim.setDescription(r.description());
        if (r.billDocumentId() != null) {
            claim.setBillDocument(documentRepository.findByIdAndDeletedFalse(r.billDocumentId())
                    .orElseThrow(() -> ApiException.badRequest("Unknown bill document: " + r.billDocumentId())));
        }
        claim.setStatus(ReimbursementStatus.SUBMITTED);
        return toResponse(claimRepository.save(claim));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReimbursementResponse> myClaims(AuthenticatedUser principal, Pageable pageable) {
        Employee employee = currentEmployee(principal);
        return PageResponse.of(claimRepository.findByEmployee_Id(employee.getId(), pageable), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReimbursementResponse> list(Long employeeId, ReimbursementStatus status, Pageable pageable) {
        if (employeeId != null && status != null) {
            return PageResponse.of(claimRepository.findByEmployee_IdAndStatus(employeeId, status, pageable), this::toResponse);
        }
        if (status != null) {
            return PageResponse.of(claimRepository.findByStatus(status, pageable), this::toResponse);
        }
        if (employeeId != null) {
            return PageResponse.of(claimRepository.findByEmployee_Id(employeeId, pageable), this::toResponse);
        }
        return PageResponse.of(claimRepository.findAll(pageable), this::toResponse);
    }

    @Override
    @Transactional
    public ReimbursementResponse approve(Long id, AuthenticatedUser principal, String remarks) {
        ReimbursementClaim claim = load(id);
        assertNotSelfApproval(claim, principal);
        OffsetDateTime now = OffsetDateTime.now();
        switch (claim.getStatus()) {
            case SUBMITTED -> {
                claim.setStatus(ReimbursementStatus.MANAGER_APPROVED);
                claim.setManagerDecisionBy(principal.email());
                claim.setManagerDecisionAt(now);
                claim.setDecisionRemarks(remarks);
            }
            case MANAGER_APPROVED -> {
                claim.setStatus(ReimbursementStatus.APPROVED);
                claim.setFinanceDecisionBy(principal.email());
                claim.setFinanceDecisionAt(now);
                claim.setDecisionRemarks(remarks);
                notify(claim, "Reimbursement approved",
                        "Your reimbursement claim of " + claim.getAmount() + " has been approved.");
            }
            default -> throw ApiException.badRequest("Claim is not awaiting approval");
        }
        return toResponse(claim);
    }

    @Override
    @Transactional
    public ReimbursementResponse reject(Long id, AuthenticatedUser principal, String remarks) {
        ReimbursementClaim claim = load(id);
        assertNotSelfApproval(claim, principal);
        if (claim.getStatus() != ReimbursementStatus.SUBMITTED
                && claim.getStatus() != ReimbursementStatus.MANAGER_APPROVED) {
            throw ApiException.badRequest("Claim cannot be rejected in its current state");
        }
        claim.setStatus(ReimbursementStatus.REJECTED);
        claim.setFinanceDecisionBy(principal.email());
        claim.setFinanceDecisionAt(OffsetDateTime.now());
        claim.setDecisionRemarks(remarks);
        notify(claim, "Reimbursement rejected",
                "Your reimbursement claim of " + claim.getAmount() + " was rejected.");
        return toResponse(claim);
    }

    @Override
    @Transactional
    public ReimbursementResponse pay(Long id, AuthenticatedUser principal) {
        ReimbursementClaim claim = load(id);
        if (claim.getStatus() != ReimbursementStatus.APPROVED) {
            throw ApiException.badRequest("Only approved claims can be paid");
        }
        claim.setStatus(ReimbursementStatus.PAID);
        claim.setPaidAt(OffsetDateTime.now());
        claim.setFinanceDecisionBy(principal.email());
        notify(claim, "Reimbursement paid",
                "Your reimbursement of " + claim.getAmount() + " has been paid.");
        return toResponse(claim);
    }

    @Override
    @Transactional
    public ReimbursementResponse cancel(Long id, AuthenticatedUser principal) {
        ReimbursementClaim claim = load(id);
        if (!isOwner(claim, principal)) {
            throw ApiException.forbidden("You can only cancel your own claim");
        }
        if (claim.getStatus() != ReimbursementStatus.SUBMITTED
                && claim.getStatus() != ReimbursementStatus.MANAGER_APPROVED) {
            throw ApiException.badRequest("Claim cannot be cancelled in its current state");
        }
        claim.setStatus(ReimbursementStatus.CANCELLED);
        return toResponse(claim);
    }

    // --- helpers ---------------------------------------------------------------------------------

    private ReimbursementClaim load(Long id) {
        return claimRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReimbursementClaim", "id", id));
    }

    private void assertNotSelfApproval(ReimbursementClaim claim, AuthenticatedUser principal) {
        if (isOwner(claim, principal)) {
            throw ApiException.forbidden("You cannot approve or reject your own reimbursement claim");
        }
    }

    private boolean isOwner(ReimbursementClaim claim, AuthenticatedUser principal) {
        Employee employee = claim.getEmployee();
        return employee.getUser() != null && employee.getUser().getId().equals(principal.id());
    }

    private void notify(ReimbursementClaim claim, String title, String message) {
        notificationService.notify(claim.getEmployee().getUser(), NotificationChannel.IN_APP, title, message,
                "REIMBURSEMENT", claim.getId());
    }

    private Employee currentEmployee(AuthenticatedUser principal) {
        return employeeRepository.findByUser_IdAndDeletedFalse(principal.id())
                .orElseThrow(() -> ApiException.badRequest("Your account is not linked to an employee profile"));
    }

    private ReimbursementResponse toResponse(ReimbursementClaim c) {
        Employee e = c.getEmployee();
        return new ReimbursementResponse(c.getId(), e.getId(), e.getFullName(), c.getCategory(), c.getAmount(),
                c.getExpenseDate(), c.getDescription(),
                c.getBillDocument() != null ? c.getBillDocument().getId() : null,
                c.getStatus(), c.getManagerDecisionBy(), c.getManagerDecisionAt(), c.getFinanceDecisionBy(),
                c.getFinanceDecisionAt(), c.getDecisionRemarks(), c.getPaidAt());
    }
}
