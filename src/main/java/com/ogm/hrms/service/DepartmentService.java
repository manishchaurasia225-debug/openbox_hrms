package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.DepartmentRequest;
import com.ogm.hrms.dto.org.DepartmentResponse;
import org.springframework.data.domain.Pageable;

/** CRUD for the Department master (RBAC module {@code DEPARTMENT}). */
public interface DepartmentService {

    DepartmentResponse create(DepartmentRequest request);

    PageResponse<DepartmentResponse> list(Pageable pageable);

    DepartmentResponse get(Long id);

    DepartmentResponse update(Long id, DepartmentRequest request);

    void delete(Long id);
}
