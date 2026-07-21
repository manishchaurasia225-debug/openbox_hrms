package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.reimbursement.ReimbursementResponse;
import com.ogm.hrms.dto.reimbursement.SubmitReimbursementRequest;
import com.ogm.hrms.enums.ReimbursementStatus;
import com.ogm.hrms.security.AuthenticatedUser;
import org.springframework.data.domain.Pageable;

/** Reimbursement / expense claims (RBAC module {@code EXPENSE}). */
public interface ReimbursementService {

    ReimbursementResponse submit(AuthenticatedUser principal, SubmitReimbursementRequest request);

    PageResponse<ReimbursementResponse> myClaims(AuthenticatedUser principal, Pageable pageable);

    PageResponse<ReimbursementResponse> list(Long employeeId, ReimbursementStatus status, Pageable pageable);

    ReimbursementResponse approve(Long id, AuthenticatedUser principal, String remarks);

    ReimbursementResponse reject(Long id, AuthenticatedUser principal, String remarks);

    ReimbursementResponse pay(Long id, AuthenticatedUser principal);

    ReimbursementResponse cancel(Long id, AuthenticatedUser principal);
}
