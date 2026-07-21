package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.employee.EmployeeRequest;
import com.ogm.hrms.dto.employee.EmployeeResponse;
import org.springframework.data.domain.Pageable;

/** Employee master management (RBAC module {@code EMPLOYEE}). */
public interface EmployeeService {

    EmployeeResponse create(EmployeeRequest request);

    PageResponse<EmployeeResponse> list(Pageable pageable);

    /** Free-text search over employee code, full name, and official email. */
    PageResponse<EmployeeResponse> search(String query, Pageable pageable);

    EmployeeResponse get(Long id);

    EmployeeResponse update(Long id, EmployeeRequest request);

    void delete(Long id);
}
