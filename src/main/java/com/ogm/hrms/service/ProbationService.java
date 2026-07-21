package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.probation.ConfirmProbationRequest;
import com.ogm.hrms.dto.probation.ExtendProbationRequest;
import com.ogm.hrms.dto.probation.ProbationResponse;
import com.ogm.hrms.dto.probation.StartProbationRequest;
import com.ogm.hrms.enums.ProbationStatus;
import com.ogm.hrms.security.AuthenticatedUser;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Confirmation & Probation (Module 13). Authorized under the {@code EMPLOYEE} RBAC module. */
public interface ProbationService {

    ProbationResponse start(StartProbationRequest request);

    PageResponse<ProbationResponse> list(Long employeeId, ProbationStatus status, Pageable pageable);

    ProbationResponse get(Long id);

    /** Active probations whose end date falls within the next {@code withinDays} days. */
    List<ProbationResponse> upcoming(int withinDays);

    ProbationResponse confirm(Long id, AuthenticatedUser principal, ConfirmProbationRequest request);

    ProbationResponse extend(Long id, AuthenticatedUser principal, ExtendProbationRequest request);

    ProbationResponse terminate(Long id, AuthenticatedUser principal, String remarks);
}
