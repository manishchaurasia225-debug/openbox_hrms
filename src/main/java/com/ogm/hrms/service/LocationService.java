package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.LocationRequest;
import com.ogm.hrms.dto.org.LocationResponse;
import org.springframework.data.domain.Pageable;

/** CRUD for the Office Location master (RBAC module {@code SETTINGS}). */
public interface LocationService {

    LocationResponse create(LocationRequest request);

    PageResponse<LocationResponse> list(Pageable pageable);

    LocationResponse get(Long id);

    LocationResponse update(Long id, LocationRequest request);

    void delete(Long id);
}
