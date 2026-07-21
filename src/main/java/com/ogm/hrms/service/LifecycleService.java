package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.lifecycle.AddLifecycleTaskRequest;
import com.ogm.hrms.dto.lifecycle.InitiateLifecycleRequest;
import com.ogm.hrms.dto.lifecycle.LifecycleCaseResponse;
import com.ogm.hrms.enums.LifecycleStatus;
import com.ogm.hrms.enums.LifecycleType;
import org.springframework.data.domain.Pageable;

/** Employee Lifecycle (Module 14). Authorized under the {@code EMPLOYEE} RBAC module. */
public interface LifecycleService {

    LifecycleCaseResponse initiate(InitiateLifecycleRequest request);

    PageResponse<LifecycleCaseResponse> list(Long employeeId, LifecycleType type, LifecycleStatus status, Pageable pageable);

    LifecycleCaseResponse get(Long id);

    LifecycleCaseResponse addTask(Long caseId, AddLifecycleTaskRequest request);

    LifecycleCaseResponse completeTask(Long caseId, Long taskId, String notes);

    LifecycleCaseResponse cancel(Long caseId);
}
