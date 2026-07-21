package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.DesignationRequest;
import com.ogm.hrms.dto.org.DesignationResponse;
import org.springframework.data.domain.Pageable;

/** CRUD for the Designation master (RBAC module {@code DESIGNATION}). */
public interface DesignationService {

    DesignationResponse create(DesignationRequest request);

    PageResponse<DesignationResponse> list(Pageable pageable);

    DesignationResponse get(Long id);

    DesignationResponse update(Long id, DesignationRequest request);

    void delete(Long id);
}
