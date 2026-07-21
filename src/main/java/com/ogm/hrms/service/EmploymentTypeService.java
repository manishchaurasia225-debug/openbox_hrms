package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.EmploymentTypeRequest;
import com.ogm.hrms.dto.org.EmploymentTypeResponse;
import org.springframework.data.domain.Pageable;

/** CRUD for the Employment Type master (RBAC module {@code SETTINGS}). */
public interface EmploymentTypeService {

    EmploymentTypeResponse create(EmploymentTypeRequest request);

    PageResponse<EmploymentTypeResponse> list(Pageable pageable);

    EmploymentTypeResponse get(Long id);

    EmploymentTypeResponse update(Long id, EmploymentTypeRequest request);

    void delete(Long id);
}
